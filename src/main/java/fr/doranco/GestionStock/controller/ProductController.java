package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.ProductRequestDTO;
import fr.doranco.GestionStock.model.dto.ProductResponseDTO;
import fr.doranco.GestionStock.model.dto.ProductUpdateDTO;
import fr.doranco.GestionStock.model.entity.Product;
import fr.doranco.GestionStock.service.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
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

    @Override
    @GetMapping("/all")
    public ResponseEntity<?> getAllProducts() throws Exception {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid product ID: " + id);
        }
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);

    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateDTO dto) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid product ID: " + id);
        }
        ProductResponseDTO updatedProduct = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updatedProduct);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid product ID: " + id);
        }
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
