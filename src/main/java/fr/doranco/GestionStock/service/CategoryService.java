package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.CategoryRequestDTO;
import fr.doranco.GestionStock.model.dto.CategoryResponseDTO;
import fr.doranco.GestionStock.model.dto.CategoryUpdateDTO;
import fr.doranco.GestionStock.model.dto.ProductResponseDTO;
import fr.doranco.GestionStock.model.entity.Category;
import fr.doranco.GestionStock.model.entity.Product;
import fr.doranco.GestionStock.repository.ICategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CategoryService implements ICategoryService {
    private final ICategoryRepository categoryRepository;

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) throws Exception {

        if(request == null){
            log.error("[CATEGORY] Creation attempt with a null request");
            throw new IllegalArgumentException("Request is null");
        }
        if(request.name() == null || request.name().isBlank()){
            throw new IllegalArgumentException("Name is null or blank");
        }
        if(categoryRepository.existsByName(request.name())){
            log.warn("[CATEGORY] Creation failed: The category '{}' already exists in the database.", request.name());
            throw new IllegalArgumentException("Category with name " + request.name() + " already exists");
        }
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
        Category savedCategory = categoryRepository.save(category);

        log.info("[CATEGORY] Succès : Catégorie créée avec l'ID {}", savedCategory.getId());

        return mapToResponseDto(savedCategory);
    }

    @Override
    public Set<CategoryResponseDTO> getAllCategories() throws Exception {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toSet());
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) throws Exception {
       if(id == null || id <= 0){
           log.warn("[CATEGORY] Retrieval failed: Invalid category ID {}", id);
           throw new IllegalArgumentException("Invalid category ID: " + id);
       }
        Category category = categoryRepository.findById(id).orElseThrow(() -> {
            log.warn("[CATEGORY] Retrieval failed: No category found with ID {}", id);
            return new IllegalArgumentException("Category with ID " + id + " not found");
        });
        return mapToResponseDto(category);
    }

    @Override
    public CategoryResponseDTO updateCategory(Long id, CategoryUpdateDTO dto) throws Exception {
        if (id == null || id <= 0) {
            log.error("[CATEGORY] Invalid ID: {}", id);
            throw new IllegalArgumentException("Invalid category ID");
        }

        if (dto == null) {
            log.error("[CATEGORY] Null request");
            throw new IllegalArgumentException("Category request cannot be null");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[CATEGORY] No category found with ID {}", id);
                    return new IllegalArgumentException("Category not found");
                });
        if (dto.name() != null && !dto.name().equals(category.getName())) {

            if (categoryRepository.existsByName(dto.name())) {
                log.warn("[CATEGORY] Update failed: Category name '{}' already exists", dto.name());
                throw new IllegalArgumentException("Category name already exists");
            }

            category.setName(dto.name());
        }

        if (dto.description() != null) {
            category.setDescription(dto.description());
        }

        Category updated = categoryRepository.save(category);

        log.info("[CATEGORY] Category {} updated successfully", id);

        return mapToResponseDto(updated);
    }

    @Override
    public void deleteCategory(Long id) throws Exception {
        if(id == null || id <= 0){
            log.warn("[CATEGORY] Deletion failed: Invalid category ID {}", id);
            throw new IllegalArgumentException("Invalid category ID: " + id);
        }
        Category category = categoryRepository.findById(id).orElseThrow(() -> {
            log.warn("[CATEGORY] Deletion failed: No category found with ID {}", id);
            return new IllegalArgumentException("Category with ID " + id + " not found");
        });
        category.setDeleted(true);
        categoryRepository.save(category);
        log.info("[CATEGORY] Category with ID {} deleted successfully", id);
    }

    private CategoryResponseDTO mapToResponseDto(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
