package fr.doranco.GestionStock.model.dto;

import java.math.BigDecimal;

public record ProductUpdateDTO
        (String reference,
        String name,
        String description,
        BigDecimal price,
        Integer actualStock,
        Integer minStock,
        Long categoryId,
         String imageUrl) {
}
