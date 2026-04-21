package fr.doranco.GestionStock.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record SaleLineRequestDTO(
        @NotNull (message ="Product must not be null") Long productId,

        @NotNull(message = "Quantity must not be null")
        @PositiveOrZero (message = "Quantity must be positive or zero")
        Integer quantity,
        @NotNull(message = "Unit price must not be null") BigDecimal unitPrice
) {
}
