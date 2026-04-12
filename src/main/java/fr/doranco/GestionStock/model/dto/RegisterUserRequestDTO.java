package fr.doranco.GestionStock.model.dto;

import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.model.entity.Role;
import fr.doranco.GestionStock.security.pwd.ValidPassword;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RegisterUserRequestDTO {


    @Size(min = 2, max = 50)
    @NotBlank(message = "name is obligatory")
    private String firstname;

    @Size(min = 2, max = 50)
    @NotBlank(message = "lastname is obligatory")
    private String lastname;

    @NotBlank(message = "username is obligatory")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "email is obligatory")
    @Size(min = 6, max = 50)
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$",
            message = "email should be valid"
    )
    private String email;

    @ValidPassword
    private String password;

    @Past(message = "birthdate must be in the past")
    private LocalDate birthdate;

    @Column(name = "phone_number", length = 20, unique = true, nullable = false)
    @NotBlank(message = "phone number is obligatory")
    @Pattern( regexp = "^(?:(?:\\+|00)33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}$", message = "Phone number must be a valid French number")
    private String phoneNumber;


    @NotEmpty(message = "role is obligatory")
    private Set<UserRole> roles;





}
