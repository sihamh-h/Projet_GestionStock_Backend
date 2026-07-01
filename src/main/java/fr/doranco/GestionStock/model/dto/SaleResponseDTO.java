package fr.doranco.GestionStock.model.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Builder
public record SaleResponseDTO(
        Long id,
        String saleNumber,
        LocalDate saleDate,
        Float totalAmount,
        List<SaleLineResponseDTO> lines,
        String createdByName
) {
}
