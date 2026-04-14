package fr.doranco.GestionStock.controller;


import fr.doranco.GestionStock.model.dto.CategoryRequestDTO;
import fr.doranco.GestionStock.model.dto.CategoryResponseDTO;
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
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
}
