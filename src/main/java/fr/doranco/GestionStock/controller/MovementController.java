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

import java.util.List;

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
        if(userDetails == null) {
            return new ResponseEntity<>("User details not found", HttpStatus.UNAUTHORIZED);
        }
        MovementResponseDTO response = movementService.createMovement(dto, userDetails.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/my-movements")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<?> getMyMovements(@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        if(userDetails == null) {
            return new ResponseEntity<>("User details not found", HttpStatus.UNAUTHORIZED);
        }
        List<MovementResponseDTO> response = movementService.getMovementsByUser(userDetails.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<?> getMovementById(@PathVariable  Long id) throws Exception {
        if(id == null || id < 0) {
            return new ResponseEntity<>("Invalid movement ID: " + id, HttpStatus.BAD_REQUEST);
        }
        MovementResponseDTO response = movementService.getMovementById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('MOVEMENT_READ')")
    public ResponseEntity<?> getAllMovements() throws Exception {
        List<MovementResponseDTO> response = movementService.getAllMovements();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
