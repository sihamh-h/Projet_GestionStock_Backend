package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.CategoryRequestDTO;
import fr.doranco.GestionStock.model.dto.CategoryResponseDTO;
import fr.doranco.GestionStock.model.entity.Category;
import fr.doranco.GestionStock.repository.ICategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        return CategoryResponseDTO.builder()
                .id(savedCategory.getId())
                .name(savedCategory.getName())
                .description(savedCategory.getDescription())
                .createdAt(savedCategory.getCreatedAt())
                .build();
    }
}
