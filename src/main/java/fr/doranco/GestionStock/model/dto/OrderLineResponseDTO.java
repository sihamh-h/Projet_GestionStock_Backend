package fr.doranco.GestionStock.model.dto;

import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record OrderLineResponseDTO(String productName,
                                   Integer quantity,
                                   BigDecimal unitPrice,
                                   BigDecimal subTotal) {
}
