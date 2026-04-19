package fr.doranco.GestionStock.controller;


import fr.doranco.GestionStock.model.dto.CategoryRequestDTO;
import fr.doranco.GestionStock.model.dto.CategoryResponseDTO;
import fr.doranco.GestionStock.model.dto.CategoryUpdateDTO;
import fr.doranco.GestionStock.model.entity.Category;
import fr.doranco.GestionStock.service.ICategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/rest/category")
@RequiredArgsConstructor
public class CategoryController implements ICategoryController {
    private final ICategoryService categoryService;

    @Override
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CATEGORY_CREATE')")
    public ResponseEntity<?> create(@Valid @RequestBody CategoryRequestDTO dto) throws Exception {
        CategoryResponseDTO created = categoryService.createCategory(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    public ResponseEntity<?> getAllCategories() throws Exception {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    public ResponseEntity<?> getCategoryById(@PathVariable  Long id) throws Exception {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Override
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_UPDATE')")
    public ResponseEntity<?> updateCategory(@PathVariable  Long id,@RequestBody CategoryUpdateDTO dto) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid category ID: " + id);
        }
        CategoryResponseDTO updated = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_DELETE')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid category ID: " + id);
        }
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
