package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.enums.MovementType;
import fr.doranco.GestionStock.enums.SourceType;
import fr.doranco.GestionStock.event.LowStockEvent;
import fr.doranco.GestionStock.model.dto.MovementRequestDTO;
import fr.doranco.GestionStock.model.dto.MovementResponseDTO;
import fr.doranco.GestionStock.model.entity.Movement;
import fr.doranco.GestionStock.model.entity.Product;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.mongo.dto.ActivityLogRequestDTO;
import fr.doranco.GestionStock.mongo.service.IActivityLogService;
import fr.doranco.GestionStock.repository.IMovementRepository;
import fr.doranco.GestionStock.repository.IProductRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import fr.doranco.GestionStock.utils.RequestUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MovementService implements IMovementService {
    private final IMovementRepository movementRepository;
    private final IProductRepository productRepository;
    private final IUserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final IActivityLogService activityLogService;

    @Override
    public MovementResponseDTO createMovement(MovementRequestDTO dto) throws Exception {
        try {
            if (dto == null) {
                log.error("[MOVEMENT] Creation attempt with a null MovementRequestDTO");
                throw new IllegalArgumentException("MovementRequestDTO cannot be null");
            }
            if (dto.getProductId() == null || dto.getProductId() < 0 || dto.getQuantity() == null || dto.getQuantity() <= 0 || dto.getType() == null) {
                log.error("[MOVEMENT] Creation attempt with invalid data: {}", dto);
                throw new IllegalArgumentException("Invalid MovementRequestDTO data: " + dto);
            }

            Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> {
                log.error("[MOVEMENT] Product with ID {} not found for movement creation", dto.getProductId());
                return new IllegalArgumentException("Product with ID " + dto.getProductId() + " not found");
            });

            Movement movement = recordMovement(product, dto.getQuantity(), dto.getType(),
                    SourceType.ADJUSTMENT);

            activityLogService.log(ActivityLogRequestDTO.builder()
                    .userId(RequestUtils.getCurrentUserId())
                    .userEmail(RequestUtils.getCurrentUserEmail())
                    .action(dto.getType() == MovementType.IN ? "MOVEMENT_IN" : "MOVEMENT_OUT")
                    .description("Recorded movement of type " + dto.getType() + " for product ID " + dto.getProductId() + " with quantity " + dto.getQuantity())
                    .success(true)
                    .build());

            return mapToResponseDTO(movement);
        }catch (Exception e) {
            log.error("[MOVEMENT] Error creating movement: {}", e.getMessage(), e);
            activityLogService.log(ActivityLogRequestDTO.builder()
                    .userId(RequestUtils.getCurrentUserId())
                    .userEmail(RequestUtils.getCurrentUserEmail())
                    .action("MOVEMENT_ERROR")
                    .description("Error creating movement: " + e.getMessage())
                    .success(false)
                    .build());
            throw e;
        }

    }


    public Movement recordMovement(Product product, Integer quantity, MovementType type,
                               SourceType source) throws Exception {

        if (type == MovementType.OUT) {
            if (product.getActualStock() < quantity) {
                log.error("[MOVEMENT] Insufficient stock for product ID {}: requested {}, available {}", product.getId(), quantity, product.getActualStock());
                throw new IllegalArgumentException("Insufficient stock for product ID " + product.getId() + ": requested " + quantity + ", available " + product.getActualStock());
            }
            product.setActualStock(product.getActualStock() - quantity);
        } else {
            product.setActualStock(product.getActualStock() + quantity);
        }
        Movement movement = Movement.builder()
                .type(type)
                .quantity(quantity)
                .product(product)
                .sourceType(source)
                .build();

        productRepository.save(product);
        movementRepository.save(movement);
        eventPublisher.publishEvent(new LowStockEvent(product));
        return movement;

    }

    @Override
    public List<MovementResponseDTO> getMovementsByUser(Long userId) throws Exception {
        if(userId == null || userId < 0) {
            log.error("[MOVEMENT] getMovementsByUser attempt with invalid userId: {}", userId);
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }
        List<Movement> movements = movementRepository.findByUserId(userId);
        if(movements.isEmpty()) {
            log.warn("[MOVEMENT] No movements found for user ID {}", userId);
        }
        return movements.stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    public List<MovementResponseDTO> getAllMovements() throws Exception {
        List<MovementResponseDTO> movements = movementRepository.findAll().stream().map(this::mapToResponseDTO).toList();
        if(movements.isEmpty()) {
            log.warn("[MOVEMENT] No movements found in the system");
         }

        return movements;
    }

    @Override
    public MovementResponseDTO getMovementById(Long id) throws Exception {
        if(id == null || id < 0) {
            log.error("[MOVEMENT] getMovementById attempt with invalid id: {}", id);
            throw new IllegalArgumentException("Invalid movement ID: " + id);
        }
        Movement movement = movementRepository.findById(id).orElseThrow(() -> {
            log.error("[MOVEMENT] Movement with ID {} not found", id);
            return new EntityNotFoundException("Movement with ID " + id + " not found");
        });
        return mapToResponseDTO(movement);
    }

    private MovementResponseDTO mapToResponseDTO(Movement movement) {
        return MovementResponseDTO.builder()
                .id(movement.getId())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .productId(movement.getProduct().getId())
                .productName(movement.getProduct().getName())
                .build();
    }
}
