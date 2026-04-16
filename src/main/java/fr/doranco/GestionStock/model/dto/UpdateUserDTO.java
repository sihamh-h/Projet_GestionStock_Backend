package fr.doranco.GestionStock.model.dto;

import fr.doranco.GestionStock.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDTO {
    @NotBlank(message = "Firstname cannot be empty")
    private String firstname;

    @NotBlank(message = "Lastname cannot be empty")
    private String lastname;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern( regexp = "^(?:(?:\\+|00)33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}$", message = "Phone number must be a valid French number")
    private String phoneNumber;

    @NotNull(message = "Birthdate is required")
    private LocalDate birthdate;

    @NotEmpty(message = "Roles cannot be empty")
    private Set<UserRole> roles;
}
