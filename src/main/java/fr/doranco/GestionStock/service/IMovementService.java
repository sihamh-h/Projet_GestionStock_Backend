package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.MovementRequestDTO;
import fr.doranco.GestionStock.model.dto.MovementResponseDTO;

public interface IMovementService {
    MovementResponseDTO createMovement(MovementRequestDTO dto,Long currentUserId) throws Exception;
}
