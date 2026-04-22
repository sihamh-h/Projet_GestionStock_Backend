package fr.doranco.GestionStock.model.dto;

import fr.doranco.GestionStock.enums.SeverityAlert;
import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record StockAlertDTO(Long id,
                            String productName,
                            String productReference,
                            SeverityAlert severity,
                            String message,
                            LocalDateTime createdAt) {
}
