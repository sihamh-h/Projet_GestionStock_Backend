package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.enums.MovementType;
import fr.doranco.GestionStock.enums.SourceType;
import fr.doranco.GestionStock.event.LowStockEvent;
import fr.doranco.GestionStock.model.dto.MovementRequestDTO;
import fr.doranco.GestionStock.model.dto.MovementResponseDTO;
import fr.doranco.GestionStock.model.dto.MovementView;
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

/**
 * Service de gestion des mouvements de stock.
 * Ce service fournit des méthodes pour créer et récupérer des mouvements de stock.
 * Il utilise les repositories IMovementRepository, IProductRepository et IUserRepository pour interagir avec la base de données.
 * Il publie également des événements de stock faible via ApplicationEventPublisher.
 * @since 2026-06-01
 * @author Siham HA
 * @version 1.0
 * @see IMovementRepository
 * @see IProductRepository
 * @see IUserRepository
 * @see ApplicationEventPublisher
 */
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

    /**
     * Crée un mouvement de stock pour un produit donné.
     * Cette méthode vérifie la validité des données fournies dans le DTO, récupère le produit correspondant, enregistre le mouvement et met à jour le stock du produit.
     * Elle publie également un événement de stock faible si nécessaire et enregistre l'action dans le journal d'activité
     * @param dto le DTO contenant les informations du mouvement à créer
     * @return un DTO représentant le mouvement créé
     * @throws Exception si le DTO est null, si les données sont invalides, si le produit n'existe pas ou en cas d'erreur lors de la création du mouvement
     * @since 2026-06-01
     * @see MovementRequestDTO
     * @see MovementResponseDTO
     * @see Product
     * @see Movement
     * @see SourceType
     * @see MovementType
     * @see IActivityLogService
     * @see RequestUtils
     * @see ApplicationEventPublisher
     */
    @Override
    public MovementResponseDTO createMovement(MovementRequestDTO dto) throws Exception {
        try {
            if (dto == null) {
                log.error("[MOVEMENT] Creation attempt with a null MovementRequestDTO");
                throw new IllegalArgumentException("MovementRequestDTO cannot be null");
            }
            if (dto.getProductId() == null || dto.getProductId() < 0 || dto.getQuantity() == null
                    || dto.getQuantity() <= 0 || dto.getType() == null) {
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

    /**
     * Enregistre un mouvement de stock pour un produit donné.
     * Cette méthode met à jour le stock du produit en fonction du type de mouvement (entrée ou sortie) et crée un enregistrement de mouvement dans la base de données.
     * Elle publie également un événement de stock faible si nécessaire.
     * @param product le produit pour lequel le mouvement est enregistré
     * @param quantity la quantité du mouvement
     * @param type le type de mouvement (entrée ou sortie)
     * @param source le type de source du mouvement (ajustement, commande, etc.)
     * @return le mouvement enregistré
     * @throws Exception si le stock est insuffisant pour un mouvement de sortie ou en cas d'erreur lors de l'enregistrement du mouvement
     * @since 2026-06-01
     * @see Product
     * @see Movement
     * @see MovementType
     */
    public Movement recordMovement(Product product, Integer quantity, MovementType type,
                               SourceType source) throws Exception {

        Long currentUserId = RequestUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId).orElse(null);

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
                .user(user)
                .sourceType(source)
                .build();

        productRepository.save(product);
        movementRepository.save(movement);
        eventPublisher.publishEvent(new LowStockEvent(product));
        return movement;

    }

    /**
     * Récupère tous les mouvements de stock associés à un utilisateur donné.
     * @param userId l'ID de l'utilisateur pour lequel récupérer les mouvements
     * @return une liste de DTO représentant les mouvements de stock de l'utilisateur
     * @throws Exception si l'ID de l'utilisateur est invalide ou en cas d'erreur lors de la récupération des mouvements
     * @since 2026-06-01
     * @see MovementResponseDTO
     * @see MovementView
     * @see IMovementRepository
     */
    @Override
    public List<MovementResponseDTO> getMovementsByUser(Long userId) throws Exception {
        if (userId == null || userId < 0) {
            log.error("[MOVEMENT] getMovementsByUser attempt with invalid userId: {}", userId);
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }
        List<MovementView> movements = movementRepository.findMovementsByUserIdWithDetails(userId);
        if (movements.isEmpty()) {
            log.warn("[MOVEMENT] No movements found for user ID {}", userId);
        }
        return movements.stream().map(this::mapViewToResponseDTO).toList();

    }

    /**
     * Récupère tous les mouvements de stock dans le système.
     * @return une liste de DTO représentant tous les mouvements de stock
     * @throws Exception en cas d'erreur lors de la récupération des mouvements
     * @since 2026-06-01
     * @see MovementResponseDTO
     * @see MovementView
     * @see IMovementRepository
     */
    @Override
    public List<MovementResponseDTO> getAllMovements() throws Exception {
        List<MovementView> movements = movementRepository.findAllMovementsWithDetails();
        if(movements.isEmpty()) {
            log.warn("[MOVEMENT] No movements found in the system");
         }
        return movements.stream().map(this::mapViewToResponseDTO).toList();
    }

    /**
     * Récupère un mouvement de stock par son ID.
     * @param id l'ID du mouvement à récupérer
     * @return un DTO représentant le mouvement de stock
     * @throws Exception si l'ID est invalide, si aucun mouvement n'est trouvé avec cet ID ou en cas d'erreur lors de la récupération du mouvement
     * @since 2026-06-01
     * @see MovementResponseDTO
     * @see Movement
     * @see IMovementRepository
     */
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

    /**
     * Mappe une vue de mouvement à un DTO de réponse.
     * @param view la vue de mouvement à mapper
     * @return un DTO représentant le mouvement de stock
     * @since 2026-06-01
     * @see MovementResponseDTO
     * @see MovementView
     * @see MovementType
     * @see SourceType
     */
    private MovementResponseDTO mapViewToResponseDTO(MovementView view) {
        String productName;
        if (view.getProductName() == null) {
            productName = "Produit introuvable";
        } else if (Boolean.TRUE.equals(view.getProductDeleted())) {
            productName = view.getProductName() + " (supprimé)";
        } else {
            productName = view.getProductName();
        }

        return MovementResponseDTO.builder()
                .id(view.getId())
                .type(MovementType.valueOf(view.getType()))
                .quantity(view.getQuantity())
                .productId(view.getProductId())
                .productName(productName)
                .sourceType(view.getSourceType() != null ? SourceType.valueOf(view.getSourceType()) : null)
                .userId(view.getUserId())
                .userFullName(view.getUserFullName() != null ? view.getUserFullName() : "—")
                .createdAt(view.getCreatedAt())
                .build();
    }

    /**
     * Mappe un mouvement à un DTO de réponse.
     * @param movement le mouvement à mapper
     * @return un DTO représentant le mouvement de stock
     * @since 2026-06-01
     * @see MovementResponseDTO
     * @see Movement
     * @see MovementType
     * @see SourceType
     */
    private MovementResponseDTO mapToResponseDTO(Movement movement) {
        return MovementResponseDTO.builder()
                .id(movement.getId())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .productId(movement.getProduct() != null ? movement.getProduct().getId() : null)
                .productName(movement.getProduct() != null ? movement.getProduct().getName() : "deleted product")
                .sourceType(movement.getSourceType())
                .userId(movement.getUser() != null ? movement.getUser().getId() : null)
                .userFullName(movement.getUser() != null
                        ? movement.getUser().getFirstname() + " " + movement.getUser().getLastname()
                        : "—")
                .createdAt(movement.getCreatedAt() != null ? movement.getCreatedAt().toString() : null)
                .build();

    }


}
