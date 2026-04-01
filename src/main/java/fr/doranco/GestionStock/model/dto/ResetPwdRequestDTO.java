package fr.doranco.GestionStock.model.dto;

import jakarta.validation.constraints.NotBlank;

public  record ResetPwdRequestDTO (@NotBlank String token, @NotBlank String password) {
}


