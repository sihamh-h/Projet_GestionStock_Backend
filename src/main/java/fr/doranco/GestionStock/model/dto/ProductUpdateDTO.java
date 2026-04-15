package fr.doranco.GestionStock.model.dto;

public record ProductUpdateDTO
        (String reference,
        String name,
        String description,
        Double price,
        Integer actualStock,
        Integer minStock,
        Long categoryId,
         String imageUrl) {
}
