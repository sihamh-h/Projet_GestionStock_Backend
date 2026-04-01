package fr.doranco.GestionStock.model.dto;

import java.time.LocalDate;

public class UserDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private LocalDate birthdate;
    private String phoneNumber;
    private LocalDate registrationDate;
    private RoleDTO role;
}
