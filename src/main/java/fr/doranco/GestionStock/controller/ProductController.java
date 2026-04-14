package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.ProductRequestDTO;
import fr.doranco.GestionStock.model.dto.ProductResponseDTO;
import fr.doranco.GestionStock.model.entity.Product;
import fr.doranco.GestionStock.service.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/rest/product")
@RequiredArgsConstructor
public class ProductController implements IProductController {
    private final IProductService productService;

    @Override
    @PutMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody ProductRequestDTO request) throws Exception {
        ProductResponseDTO product = productService.createProduct(request);
        return new ResponseEntity<>(request, HttpStatus.CREATED);
    }
}
