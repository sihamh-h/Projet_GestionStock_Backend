package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.enums.OrderStatus;
import fr.doranco.GestionStock.model.dto.OrderRequestDTO;
import fr.doranco.GestionStock.model.dto.OrderResponseDTO;
import fr.doranco.GestionStock.model.dto.ProductResponseDTO;
import fr.doranco.GestionStock.security.CustomUserDetails;
import fr.doranco.GestionStock.service.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rest/order")
@RequiredArgsConstructor
public class OrderController implements IOrderController {
    private final IOrderService orderService;
    @Override
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ORDER_CREATE')")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequestDTO dto,  @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        if(dto == null){
            return ResponseEntity.badRequest().body("Request is null");
        }
        OrderResponseDTO order = orderService.create(dto, userDetails.getId());
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<?> getOrderById(@PathVariable  Long id) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid order ID: " + id);
        }
        return ResponseEntity.ok(orderService.getById(id));
    }

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<?> getAll() throws Exception {
        return ResponseEntity.ok(orderService.getAll());
    }

    @Override
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ORDER_UPDATE')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable  Long id, @RequestParam OrderStatus status) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid order ID: " + id);
        }
        if(status == null){
            return ResponseEntity.badRequest().body("Status must be provided");
        }
        OrderResponseDTO updatedOrder = orderService.updateStatus(id, status);
        return ResponseEntity.ok(updatedOrder);

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_DELETE')")
    @Override
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid order ID: " + id);
        }
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
