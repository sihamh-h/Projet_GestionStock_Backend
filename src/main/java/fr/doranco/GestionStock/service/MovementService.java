package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.enums.MovementType;
import fr.doranco.GestionStock.model.dto.MovementRequestDTO;
import fr.doranco.GestionStock.model.dto.MovementResponseDTO;
import fr.doranco.GestionStock.model.entity.Movement;
import fr.doranco.GestionStock.model.entity.Product;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.repository.IMovementRepository;
import fr.doranco.GestionStock.repository.IProductRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public MovementResponseDTO createMovement(MovementRequestDTO dto, Long currentUserId) throws Exception {
        if(dto == null) {
            log.error("[MOVEMENT] Creation attempt with a null MovementRequestDTO");
            throw new IllegalArgumentException("MovementRequestDTO cannot be null");
        }
        if(dto.getProductId() == null || dto.getProductId() < 0 || dto.getQuantity() == null || dto.getQuantity() <= 0 || dto.getType() == null) {
            log.error("[MOVEMENT] Creation attempt with invalid data: {}", dto);
            throw new IllegalArgumentException("Invalid MovementRequestDTO data: " + dto);
        }

        Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> {
            log.error("[MOVEMENT] Product with ID {} not found for movement creation", dto.getProductId());
            return new IllegalArgumentException("Product with ID " + dto.getProductId() + " not found");
        });

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> {
                    log.error("[MOVEMENT] User with ID {} not found for movement creation", currentUserId);
                    return new IllegalArgumentException("User with ID " + currentUserId + " not found");
                });

        int quantity = dto.getQuantity();
        if(dto.getType() == MovementType.OUT) {
            if(product.getActualStock() < quantity){
                log.error("[MOVEMENT] Insufficient stock for product ID {}: requested {}, available {}", dto.getProductId(), quantity, product.getActualStock());
                throw new IllegalArgumentException("Insufficient stock for product ID " + dto.getProductId() + ": requested " + quantity + ", available " + product.getActualStock());
            }
            product.setActualStock((product.getActualStock() - quantity));
        }
        else {
            product.setActualStock((product.getActualStock() + quantity));
        }
        Movement movement = Movement.builder()
                .type(dto.getType())
                .quantity(quantity)
                .product(product)
                .user(user)
                .build();

        movementRepository.save(movement);
        productRepository.save(product);

        return mapToResponseDTO(movement);
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
