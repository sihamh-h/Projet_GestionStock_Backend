package fr.doranco.GestionStock.service;


import fr.doranco.GestionStock.enums.MovementType;
import fr.doranco.GestionStock.enums.OrderStatus;
import fr.doranco.GestionStock.enums.SourceType;
import fr.doranco.GestionStock.model.dto.*;
import fr.doranco.GestionStock.model.entity.*;
import fr.doranco.GestionStock.repository.IOrderRepository;
import fr.doranco.GestionStock.repository.IProductRepository;
import fr.doranco.GestionStock.repository.ISupplierRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des commandes.
 * Ce service fournit des méthodes pour créer, récupérer, mettre à jour et supprimer des commandes.
 * Il utilise les repositories IOrderRepository, ISupplierRepository, IUserRepository et IProductRepository pour interagir avec la base de données.
 * Il utilise également le service MovementService pour gérer les mouvements de stock lors de la réception des commandes.
 *
 * @since 2026-06-01
 * @author Siham HA
 * @version 1.0
 * @see IOrderRepository
 * @see ISupplierRepository
 * @see IUserRepository
 * @see IProductRepository
 * @see MovementService
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService implements IOrderService {

    private final IOrderRepository orderRepository;
    private final ISupplierRepository supplierRepository;
    private final IUserRepository userRepository;
    private final IProductRepository productRepository;
    private final MovementService movementService;

    /**
     * Crée une nouvelle commande.
     * @param dto les données de la commande à créer
     * @param userId l'identifiant de l'utilisateur qui crée la commande
     * @return les données de la commande créée
     * @throws Exception si les données de la commande sont invalides, si le fournisseur ou l'utilisateur n'existe pas, ou si un produit de la commande n'existe pas
     * @since 2026-06-01
     * @see IOrderRepository
     * @see ISupplierRepository
     * @see IUserRepository
     * @see IProductRepository
     * @see MovementService
     */
    @Override
    public OrderResponseDTO create(OrderRequestDTO dto, Long userId) throws Exception {
        if(dto == null){
            log.error("[ORDER] Creation attempt with a null request");
            throw new IllegalArgumentException("Request is null");
        }
        if(dto.supplierId() == null || userId == null || dto.lines() == null || dto.lines().isEmpty() || dto.orderNumber().isEmpty()) {
            log.error("[ORDER] Creation attempt with invalid data: orderNumber={}, supplierId={}, userId={}, lines={}",dto.orderNumber(),  dto.supplierId(), userId, dto.lines());
            throw new IllegalArgumentException("Invalid order data: orderNumber,  supplierId, userId and lines must be provided and valid");
        }

        Supplier supplier = supplierRepository.findById(dto.supplierId())
                .orElseThrow(() -> {
                    log.warn("[ORDER] Creation failed: No supplier found with ID {}", dto.supplierId());
                    return new EntityNotFoundException("Supplier not found");
                });
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[ORDER] Creation failed: No user found with ID {}", userId);
                    return new EntityNotFoundException("User not found");
                });
        Order order = Order.builder()
                .orderNumber(dto.orderNumber())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDate.now())
                .supplier(supplier)
                .user(user)
                .lines(new ArrayList<>())
                .build();
        float total = 0;
        for (OrderLineRequestDTO lineDto : dto.lines()) {
            log.info("[ORDER] Processing line: productId={}, quantity={}, unitPrice={}", lineDto.productId(), lineDto.quantity(), lineDto.unitPrice());
            Product product = productRepository.findById(lineDto.productId())
                    .orElseThrow(() -> {
                        log.warn("[ORDER] Product not found with ID {}", lineDto.productId());
                        return new IllegalArgumentException("Product not found ID: " + lineDto.productId());
                    });
            OrderLine line = OrderLine.builder()
                    .product(product)
                    .quantity(lineDto.quantity())
                    .unitPrice(lineDto.unitPrice())
                    .order(order)
                    .build();

            order.getLines().add(line);
            total += lineDto.quantity() * lineDto.unitPrice().floatValue();
        }

        order.setTotalAmount(total);
        log.info("[ORDER] Success: Order created with order number {}", order.getOrderNumber());
        return mapToResponse(orderRepository.save(order));

    }

    /**
     * Récupère une commande par son identifiant.
     * @param id l'identifiant de la commande à récupérer
     * @return les données de la commande récupérée
     * @throws Exception si l'identifiant est invalide ou si aucune commande n'est trouvée avec cet identifiant
     * @since 2026-06-01
     * @see IOrderRepository
     * @see ISupplierRepository
     * @see IUserRepository
     * @see IProductRepository
     * @see MovementService
     */
    @Override
    public OrderResponseDTO getById(Long id) throws Exception {
        if(id == null || id < 0){
            log.error("[ORDER] Retrieval attempt with invalid ID: {}", id);
            throw new IllegalArgumentException("Invalid order ID: " + id);
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[ORDER] Retrieval failed: No order found with ID {}", id);
                    return new EntityNotFoundException("Order not found");
                });
        return mapToResponse(order);
    }

    /**
     * Récupère toutes les commandes.
     * @return une liste des données de toutes les commandes
     * @since 2026-06-01
     * @see IOrderRepository
     * @see ISupplierRepository
     * @see IUserRepository
     * @see IProductRepository
     * @see MovementService
     */
    @Override
    public List<OrderResponseDTO> getAll() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Met à jour le statut d'une commande.
     * @param id l'identifiant de la commande à mettre à jour
     * @param status le nouveau statut de la commande
     * @return les données de la commande mise à jour
     * @throws Exception si l'identifiant est invalide, si aucune commande n'est trouvée avec cet identifiant, ou si le statut de la commande ne peut pas être mis à jour
     * @since 2026-06-01
     * @see IOrderRepository
     * @see ISupplierRepository
     * @see IUserRepository
     * @see IProductRepository
     * @see MovementService
     */
    @Override
    public OrderResponseDTO updateStatus(Long id, OrderStatus status) throws Exception {
        if(id == null || id < 0){
            log.error("[ORDER] Status update attempt with invalid ID: {}", id);
            throw new IllegalArgumentException("Invalid order ID: " + id);
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[ORDER] Status update failed: No order found with ID {}", id);
                    return new EntityNotFoundException("Order not found");
                });

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be updated");
        }

        if(status == OrderStatus.RECEIVED) {
            log.info("[ORDER] Updating stock for order ID {}: status changed to RECEIVED", id);
            for (OrderLine line : order.getLines()) {
                Product product = productRepository
                        .findByIdIncludingDeleted(line.getProduct().getId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Product not found ID: " + line.getProduct().getId()));

                if (product.isDeleted()) {
                    log.error("[ORDER] Product '{}' is deleted, cannot receive order ID {}",
                            product.getName(), id);
                    throw new IllegalStateException(
                            "Le produit '" + product.getName() + "' est supprimé. " +
                                    "Veuillez le restaurer avant de réceptionner cette commande.");
                }

                movementService.recordMovement(
                        product,
                        line.getQuantity(),
                        MovementType.IN,
                        SourceType.ORDER
                );
            }
        }
        order.setStatus(status);

        return mapToResponse(orderRepository.save(order));
    }

    /**
     * Supprime une commande par son identifiant.
     * @param id l'identifiant de la commande à supprimer
     * @throws Exception si l'identifiant est invalide, si aucune commande n'est trouvée avec cet identifiant, ou si la commande a déjà été reçue
     * @since 2026-06-01
     * @see IOrderRepository
     */
    @Override
    public void delete(Long id) throws Exception {
        if(id == null || id < 0){
            log.error("[ORDER] Deletion attempt with invalid ID: {}", id);
            throw new IllegalArgumentException("Invalid order ID: " + id);
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[ORDER] Deletion failed: No order found with ID {}", id);
                    return new EntityNotFoundException("Order not found");
                });

        if (order.getStatus() == OrderStatus.RECEIVED) {
            throw new IllegalStateException("Cannot delete an order that has already been received");
        }

        order.setDeleted(true);
        orderRepository.save(order);

    }

    /**
     * Mappe une entité Order vers un DTO OrderResponseDTO.
     * @param order l'entité Order à mapper
     * @return le DTO OrderResponseDTO correspondant
     * @since 2026-06-01
     * @see Order
     * @see OrderResponseDTO
     * @see IUserRepository
     */
    private OrderResponseDTO mapToResponse(Order order) {
        String createdByName = "—";
        if (order.getCreatedBy() != null) {
            createdByName = userRepository.findById(order.getCreatedBy())
                    .map(u -> u.getFirstname() + " " + u.getLastname())
                    .orElse("—");
        }
        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .createdByName(createdByName)
                .status(order.getStatus())
                .supplierName(order.getSupplier().getName())
                .lines(order.getLines().stream().map(line -> {
                    BigDecimal subTotal = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));

                    String productName = productRepository
                            .findByIdIncludingDeleted(line.getProduct().getId())
                            .map(p -> p.isDeleted() ? p.getName() + " (supprimé)" : p.getName())
                            .orElse("Produit introuvable");

                    return OrderLineResponseDTO.builder()
                            .productName(productName)
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .subTotal(subTotal)
                            .build();
                }).toList())
                .build();
    }
    }
