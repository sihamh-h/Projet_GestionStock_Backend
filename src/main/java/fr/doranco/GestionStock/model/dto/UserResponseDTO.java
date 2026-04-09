package fr.doranco.GestionStock.model.dto;


import fr.doranco.GestionStock.enums.UserRole;

import lombok.*;

import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private Set<String> roles;
    private String token;
    private String refreshToken;
}
