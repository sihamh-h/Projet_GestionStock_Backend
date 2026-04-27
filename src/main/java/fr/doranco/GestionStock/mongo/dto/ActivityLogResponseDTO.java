package fr.doranco.GestionStock.mongo.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ActivityLogResponseDTO(String id,
                                     Long userId,
                                     String userEmail,
                                     String action,
                                     String description,
                                     LocalDateTime timestamp,
                                     boolean success) {
}
