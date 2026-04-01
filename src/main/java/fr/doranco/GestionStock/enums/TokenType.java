package fr.doranco.GestionStock.enums;

import lombok.Getter;

@Getter
public enum TokenType {
    ACCESS,
    ACTIVATION,
    PASSWORD_RESET,
    REFRESH,
    CONFIRMATION
}
