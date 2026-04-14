package fr.doranco.GestionStock.model.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ProductResponseDTO(Long id,
                                 String reference,
                                 String name,
                                 String description,
                                 Double price,
                                 Integer actualStock,
                                 Integer minStock,
                                 String imageUrl,
                                 Long categoryId,
                                 String categoryName,
                                 LocalDateTime createdAt) {
}
