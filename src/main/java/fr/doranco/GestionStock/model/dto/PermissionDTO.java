package fr.doranco.GestionStock.model.dto;

import fr.doranco.GestionStock.model.entity.Role;

import java.util.List;

public class PermissionDTO {
    private Long id;
    private String name;
    private String description;
    private List<RoleDTO> roles;
}
