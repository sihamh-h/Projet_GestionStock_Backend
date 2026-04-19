package fr.doranco.GestionStock.model.dto;

import fr.doranco.GestionStock.enums.OrderStatus;

import java.time.LocalDate;
import java.util.List;

public record OrderResponseDTO(Long id,
                               String orderNumber,
                               OrderStatus status,
                               LocalDate orderDate,
                               Float totalAmount,
                               String supplierName,
                               List<OrderLineResponseDTO> lines) {
}
