package fr.doranco.GestionStock.model.dto;

import fr.doranco.GestionStock.model.entity.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private List<UserDTO> users;
    private List<PermissionDTO> permissions;
}
