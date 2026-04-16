package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.MovementRequestDTO;
import fr.doranco.GestionStock.model.dto.MovementResponseDTO;
import fr.doranco.GestionStock.security.CustomUserDetails;
import fr.doranco.GestionStock.service.IMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/rest/movement")
@RequiredArgsConstructor
public class MovementController implements  IMovementController {
    private final IMovementService movementService;
    @Override
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('MOVEMENT_RECORD')")
    public ResponseEntity<?> createMovement(@Valid @RequestBody MovementRequestDTO dto, @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        MovementResponseDTO response = movementService.createMovement(dto, userDetails.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }



}
