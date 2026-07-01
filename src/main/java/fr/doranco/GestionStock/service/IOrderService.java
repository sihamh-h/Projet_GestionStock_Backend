package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.enums.OrderStatus;
import fr.doranco.GestionStock.model.dto.OrderRequestDTO;
import fr.doranco.GestionStock.model.dto.OrderResponseDTO;

import java.util.List;

public interface IOrderService {
    OrderResponseDTO create(OrderRequestDTO dto, Long userId) throws Exception;
    OrderResponseDTO getById(Long id) throws Exception;
    List<OrderResponseDTO> getAll();
    OrderResponseDTO updateStatus(Long id, OrderStatus status) throws Exception;
    void delete(Long id) throws Exception;
}
