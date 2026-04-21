package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.SaleRequestDTO;
import fr.doranco.GestionStock.model.dto.SaleResponseDTO;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.service.ISaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/rest/sales")
@RequiredArgsConstructor
public class SaleController implements ISaleController {

    private final ISaleService saleService;

     @Override
     @PostMapping("/create")
     @PreAuthorize("hasAnyAuthority('SALE_CREATE', 'MOVEMENT_RECORD')")
    public ResponseEntity<?> create(@Valid @RequestBody SaleRequestDTO dto,@AuthenticationPrincipal User currentUser) throws Exception {
        SaleResponseDTO response = saleService.createSale(dto, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
