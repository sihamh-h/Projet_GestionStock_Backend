package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.LoginRequestDTO;
import fr.doranco.GestionStock.model.dto.RegisterUserRequestDTO;
import fr.doranco.GestionStock.model.dto.UserResponseDTO;

public interface IAuthService {
    UserResponseDTO login(LoginRequestDTO user) throws Exception;

    UserResponseDTO registerUser(RegisterUserRequestDTO registerUserDTO) throws Exception;

    void resetPwd(String token, String newPassword) throws Exception;

    UserResponseDTO refreshToken(String refreshToken) throws Exception;

    void initiatePasswordReset(String email) throws Exception;
}

