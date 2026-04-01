package fr.doranco.GestionStock.model.entity;


import fr.doranco.GestionStock.security.pwd.ValidPassword;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users")
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor

public class User extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    @Size(min = 2, max = 50)
    @NotBlank(message = "name is obligatory")
    private String firstname;

    @Column(nullable = false, length = 50)
    @Size(min = 2, max = 50)
    @NotBlank(message = "lastname is obligatory")
    private String lastname;

    @Column(nullable = false, length = 50, unique = true)
    @NotBlank(message = "username is obligatory")
    @Size(min = 3, max = 50)
    private String username;

    @Column(nullable = false, length = 50, unique = true)
    @NotBlank(message = "email is obligatory")
    @Size(min = 6, max = 50)
    //@Email(message = "email should be valid")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$",
            message = "email should be valid"
    )
    private String email;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "password is obligatory")
    //@Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{12,}$", message = "Password must be at least 12 characters, contain uppercase, lowercase, number, and special character")
    //@ValidPassword
    private String password;

    @Past(message = "birthdate must be in the past")
    private LocalDate birthdate;

    @Column(name = "phone_number", length = 20, unique = true, nullable = false)
    @NotBlank(message = "phone number is obligatory")
    @Pattern( regexp = "^(?:(?:\\+|00)33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}$", message = "Phone number must be a valid French number")
    private String phoneNumber;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    private boolean enabled = false;

    @Builder.Default
    @Column(name = "must_change_password")
    private boolean mustChangePassword = true;




}
