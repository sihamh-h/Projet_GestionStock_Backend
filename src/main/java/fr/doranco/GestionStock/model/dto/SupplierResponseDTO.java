package fr.doranco.GestionStock.model.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SupplierResponseDTO(Long id,
                                  String number,
                                  String name,
                                  String companyName,
                                  String email,
                                  String phone,
                                  LocalDateTime createdAt,
                                  Long createdBy,
                                  String createdByName
) {
}
