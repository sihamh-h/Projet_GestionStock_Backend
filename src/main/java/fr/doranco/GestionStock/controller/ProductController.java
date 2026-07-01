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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/rest/product")
@RequiredArgsConstructor
public class ProductController implements IProductController {
    private final IProductService productService;

    @Override
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<?> create(@Valid @RequestBody ProductRequestDTO request) throws Exception {
        ProductResponseDTO product = productService.createProduct(request);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<?> getAllProducts() throws Exception {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<?> getProductById(@PathVariable Long id) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid product ID: " + id);
        }
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);

    }

    @Override
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateDTO dto) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid product ID: " + id);
        }
        ProductResponseDTO updatedProduct = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updatedProduct);
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid product ID: " + id);
        }
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<?> restoreProduct(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(productService.restoreProduct(id));
    }

    @Override
    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<?> getDeletedProducts() throws Exception {
        return ResponseEntity.ok(productService.getDeletedProducts());
    }
}
