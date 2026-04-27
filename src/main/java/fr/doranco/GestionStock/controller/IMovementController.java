package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.MovementRequestDTO;
import fr.doranco.GestionStock.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;

public interface IMovementController {
    ResponseEntity<?> createMovement(MovementRequestDTO dto) throws Exception;
    ResponseEntity<?> getMyMovements (CustomUserDetails userDetails) throws Exception;
    ResponseEntity<?> getMovementById(Long id) throws Exception;
    ResponseEntity<?> getAllMovements() throws Exception;
}
