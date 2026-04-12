package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.RegisterUserRequestDTO;
import fr.doranco.GestionStock.model.dto.UserCreateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface IUserController {

    ResponseEntity<?> createUser(UserCreateDTO user)throws Exception;
    ResponseEntity<String> deleteUser( Long id) throws Exception;
}
