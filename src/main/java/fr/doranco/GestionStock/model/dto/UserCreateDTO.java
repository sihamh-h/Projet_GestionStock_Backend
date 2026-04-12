package fr.doranco.GestionStock.model.dto;

import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.security.pwd.ValidPassword;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDTO {
    @Size(min = 2, max = 50)
    @NotBlank(message = "firstname is obligatory")
    private String firstname;

    @Size(min = 2, max = 50)
    @NotBlank(message = "lastname is obligatory")
    private String lastname;

    @Size(min = 6, max = 50)
    @Email(message = "invalid email format")
    @NotBlank(message = "email is obligatory")
    private String email;

    @NotBlank(message = "username is obligatory")
    @Size(min = 3, max = 50)
    private String username;

    @Column(name = "phone_number", length = 20, unique = true, nullable = false)
    @NotBlank(message = "phone number is obligatory")
    @Pattern( regexp = "^(?:(?:\\+|00)33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}$", message = "Phone number must be a valid French number")
    private String phoneNumber;

    @Past(message = "birthdate must be in the past")
    private LocalDate birthdate;


    @NotEmpty(message = "role is obligatory")
    private Set<UserRole> roles;
}
