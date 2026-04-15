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
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/rest/category")
@RequiredArgsConstructor
public class CategoryController implements ICategoryController {
    private final ICategoryService categoryService;

    @Override
    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody CategoryRequestDTO dto) throws Exception {
        CategoryResponseDTO created = categoryService.createCategory(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/all")
    public ResponseEntity<?> getAllCategories() throws Exception {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable  Long id) throws Exception {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable  Long id,@RequestBody CategoryUpdateDTO dto) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid category ID: " + id);
        }
        CategoryResponseDTO updated = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) throws Exception {
        if(id < 0){
            return ResponseEntity.badRequest().body("Invalid category ID: " + id);
        }
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
