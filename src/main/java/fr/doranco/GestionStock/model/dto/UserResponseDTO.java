package fr.doranco.GestionStock.model.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import fr.doranco.GestionStock.enums.UserRole;

import lombok.*;

import java.time.LocalDate;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private Set<String> roles;
    private String token;
    private String refreshToken;
    private String phoneNumber;
    private LocalDate birthdate;
    private String imageUrl;
}
