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

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('SALE_READ')")
    public ResponseEntity<?> getAll() throws Exception {
        return ResponseEntity.ok(saleService.getAll());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALE_READ')")
    public ResponseEntity<?> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(saleService.getById(id));
    }
}
