package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.ChangePasswordRequestDTO;
import fr.doranco.GestionStock.model.dto.RegisterUserRequestDTO;
import fr.doranco.GestionStock.model.dto.UpdateUserDTO;
import fr.doranco.GestionStock.model.dto.UserCreateDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface IUserController {

    ResponseEntity<?> createUser(UserCreateDTO user)throws Exception;
    ResponseEntity<?> deleteUser( Long id) throws Exception;
    ResponseEntity<?> getUserById( Long id) throws Exception;
    ResponseEntity<?> getUsers() throws Exception;
    ResponseEntity<?> updateUser( Long id, UpdateUserDTO dto) throws Exception;
    ResponseEntity<?> changePassword(Long id, ChangePasswordRequestDTO dto) throws Exception;

}
