package fr.doranco.GestionStock.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDTO (@NotBlank
@Size String name, String description){
}
