package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.MovementRequestDTO;
import fr.doranco.GestionStock.model.dto.MovementResponseDTO;

import java.util.List;

public interface IMovementService {
    MovementResponseDTO createMovement(MovementRequestDTO dto,Long currentUserId) throws Exception;
    List<MovementResponseDTO> getMovementsByUser(Long userId) throws Exception;
    List<MovementResponseDTO> getAllMovements() throws Exception;
    MovementResponseDTO getMovementById(Long id) throws Exception;
}
