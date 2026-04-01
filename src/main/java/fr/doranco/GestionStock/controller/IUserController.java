package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.RegisterUserRequestDTO;
import fr.doranco.GestionStock.model.dto.UserResponseDTO;
import org.springframework.http.ResponseEntity;

public interface IUserController {

    ResponseEntity<UserResponseDTO> registerUser(RegisterUserRequestDTO user)throws Exception;
}
