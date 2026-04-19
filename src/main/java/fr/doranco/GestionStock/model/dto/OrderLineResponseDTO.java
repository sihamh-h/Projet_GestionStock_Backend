package fr.doranco.GestionStock.model.dto;

import java.math.BigDecimal;

public record OrderLineResponseDTO(String productName,
                                   Integer quantity,
                                   BigDecimal unitPrice) {
}
