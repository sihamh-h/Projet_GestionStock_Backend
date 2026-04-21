package fr.doranco.GestionStock.model.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SaleRequestDTO(@NotEmpty(message = "Sale number must not be empty") String saleNumber,
                             List<SaleLineRequestDTO> lines) {
}
