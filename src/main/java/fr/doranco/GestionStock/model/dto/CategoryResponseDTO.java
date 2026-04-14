package fr.doranco.GestionStock.model.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record CategoryResponseDTO(
                                  Long id,
                                  String name,
                                  String description,
                                  LocalDateTime createdAt ){
}
