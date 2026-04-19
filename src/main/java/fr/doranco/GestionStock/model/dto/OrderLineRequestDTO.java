package fr.doranco.GestionStock.model.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderLineRequestDTO(@NotNull(message="product Id must be not not null") Long productId,
                                  @NotNull (message =" Quantity must not be null") Integer quantity,
                                  @NotNull(message = "Unit price must not be null") BigDecimal unitPrice) {
}
