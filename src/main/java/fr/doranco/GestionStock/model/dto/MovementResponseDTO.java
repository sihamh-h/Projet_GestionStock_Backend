package fr.doranco.GestionStock.model.dto;

import fr.doranco.GestionStock.enums.MovementType;
import fr.doranco.GestionStock.enums.SourceType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MovementResponseDTO {
    private Long id;
    private MovementType type;
    private Integer quantity;
    private Long productId;
    private String productName;
    private SourceType sourceType;
    private Long userId;
    private String userFullName;
    private String createdAt;

}
