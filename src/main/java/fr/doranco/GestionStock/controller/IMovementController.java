package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.MovementRequestDTO;
import fr.doranco.GestionStock.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;

public interface IMovementController {
    ResponseEntity<?> createMovement(MovementRequestDTO dto, CustomUserDetails userDetails) throws Exception;
}
