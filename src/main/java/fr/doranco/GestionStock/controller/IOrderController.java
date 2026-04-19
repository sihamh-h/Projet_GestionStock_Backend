package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.enums.OrderStatus;
import fr.doranco.GestionStock.model.dto.OrderRequestDTO;
import org.springframework.http.ResponseEntity;

public interface IOrderController {
    ResponseEntity<?> createOrder(OrderRequestDTO dto) throws Exception;
    ResponseEntity<?> getOrderById(Long id) throws Exception;
    ResponseEntity<?> getAll() throws Exception;
    ResponseEntity<?> updateOrderStatus(Long id, OrderStatus status) throws Exception;
    ResponseEntity<?> deleteOrder(Long id) throws Exception;

}
