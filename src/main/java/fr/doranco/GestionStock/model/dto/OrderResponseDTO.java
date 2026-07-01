package fr.doranco.GestionStock.model.dto;

import fr.doranco.GestionStock.enums.OrderStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder

public record OrderResponseDTO(Long id,
                               String orderNumber,
                               OrderStatus status,
                               LocalDate orderDate,
                               Float totalAmount,
                               String supplierName,
                               String createdByName,
                               List<OrderLineResponseDTO> lines)
{
}
