package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.ProductResponseDTO;
import fr.doranco.GestionStock.model.dto.SupplierRequestDTO;
import fr.doranco.GestionStock.model.dto.SupplierResponseDTO;
import fr.doranco.GestionStock.service.ISupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/rest/supplier")
@RequiredArgsConstructor
public class SupplierController implements ISupplierController {
    private final ISupplierService supplierService;

    @Override
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('SUPPLIER_CREATE')")
    public ResponseEntity<?> create(@Valid @RequestBody  SupplierRequestDTO request) throws Exception {
        SupplierResponseDTO response = supplierService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    public ResponseEntity<?> getAll() throws Exception {
        return ResponseEntity.ok(supplierService.getAll());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    public ResponseEntity<?> getById(@PathVariable  Long id) throws Exception {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_UPDATE')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SupplierRequestDTO request) throws Exception {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_DELETE')")
    public ResponseEntity<?> delete(@PathVariable Long id) throws Exception {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
