package fr.doranco.GestionStock.model.dto;

import fr.doranco.GestionStock.enums.MovementType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MovementRequestDTO {

    @NotNull(message = "movement type must not be null")
    @Enumerated(EnumType.STRING)
    private MovementType type;

    @NotNull
    @Positive(message = "quantity must be positive")
    private Integer quantity;

    @NotNull(message = "productId must not be null")
    private Long productId;
}
