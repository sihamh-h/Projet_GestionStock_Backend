package fr.doranco.GestionStock.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequestDTO(@NotBlank(message = "Reference is required")
                                String reference,

                                @NotBlank(message = "Name is required")
                                String name,

                                String description,

                                @NotNull(message = "Price is required")
                                @PositiveOrZero(message = "Price must be positive")
                                Double price,

                                @NotNull(message = "Stock is required")
                                @PositiveOrZero(message = "Stock cannot be negative")
                                Integer actualStock,

                                @NotNull(message = "Minimum stock is required")
                                @PositiveOrZero(message = "Minimum stock cannot be negative")
                                Integer minStock,

                                String imageUrl,

                                @NotNull(message = "Category ID is required")
                                Long categoryId) {

}
