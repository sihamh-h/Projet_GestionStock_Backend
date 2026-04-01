package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.LoginRequestDTO;
import fr.doranco.GestionStock.model.dto.RegisterUserRequestDTO;
import fr.doranco.GestionStock.model.dto.ResetPwdRequestDTO;
import fr.doranco.GestionStock.model.dto.UserResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.RequestBody;

public interface IAuthController {
    ResponseEntity<?> login(LoginRequestDTO user)throws BadCredentialsException, DisabledException, Exception;
    ResponseEntity<?> registerUser(RegisterUserRequestDTO user)throws Exception;
    ResponseEntity<?> resetPassword(@RequestBody ResetPwdRequestDTO request) throws Exception;

}
