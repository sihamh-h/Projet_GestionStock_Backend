package fr.doranco.GestionStock.service;


import fr.doranco.GestionStock.enums.MovementType;
import fr.doranco.GestionStock.enums.OrderStatus;
import fr.doranco.GestionStock.enums.SourceType;
import fr.doranco.GestionStock.model.dto.OrderLineRequestDTO;
import fr.doranco.GestionStock.model.dto.OrderLineResponseDTO;
import fr.doranco.GestionStock.model.dto.OrderRequestDTO;
import fr.doranco.GestionStock.model.dto.OrderResponseDTO;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    @Override
    public OrderResponseDTO create(OrderRequestDTO dto) throws Exception {
        if(dto == null){
            log.error("[ORDER] Creation attempt with a null request");
            throw new IllegalArgumentException("Request is null");
        }
        if(dto.supplierId() == null || dto.userId() == null || dto.lines() == null || dto.lines().isEmpty() || dto.orderNumber().isEmpty()) {
            log.error("[ORDER] Creation attempt with invalid data: orderNumber={}, supplierId={}, userId={}, lines={}",dto.orderNumber(),  dto.supplierId(), dto.userId(), dto.lines());
            throw new IllegalArgumentException("Invalid order data: orderNumber,  supplierId, userId and lines must be provided and valid");
        }

        Supplier supplier = supplierRepository.findById(dto.supplierId())
                .orElseThrow(() -> {
                    log.warn("[ORDER] Creation failed: No supplier found with ID {}", dto.supplierId());
                    return new EntityNotFoundException("Supplier not found");
                });
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> {
                    log.warn("[ORDER] Creation failed: No user found with ID {}", dto.userId());
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
                    .orElseThrow(() -> new EntityNotFoundException("Product not found ID: " + lineDto.productId()));

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

    @Override
    public List<OrderResponseDTO> getAll() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

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
                movementService.recordMovement(
                        line.getProduct(),
                        line.getQuantity(),
                        MovementType.IN,
                        SourceType.ORDER,
                        order.getUser()
                );
            }
        }
        order.setStatus(status);

        return mapToResponse(orderRepository.save(order));
    }

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

    private OrderResponseDTO mapToResponse(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getSupplier().getName(),
                order.getLines().stream()
                        .map(l -> new OrderLineResponseDTO(l.getProduct().getName(), l.getQuantity(), l.getUnitPrice()))
                        .toList()
        );
    }
}
