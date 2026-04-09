package fr.doranco.GestionStock.model.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(@NotBlank  String refreshToken) {
}
