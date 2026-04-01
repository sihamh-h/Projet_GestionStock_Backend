package fr.doranco.GestionStock.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ROLE_ADMIN("Administrateur avec tous les droits"),
    ROLE_MANAGER("Responsable des stocks et inventaires"),
    ROLE_EMPLOYEE("Consultation et opérations de base");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

}
