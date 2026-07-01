package fr.doranco.GestionStock.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequestDTO(@NotBlank(message = "Order number must not be blank") String orderNumber,
                              @NotNull(message = "Supplier must not be null") Long supplierId,
                              @NotEmpty(message = "Order must have at least one line") List<OrderLineRequestDTO> lines) {
}
