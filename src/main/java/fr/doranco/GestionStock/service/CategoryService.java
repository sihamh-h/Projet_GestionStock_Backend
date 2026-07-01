package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.CategoryRequestDTO;
import fr.doranco.GestionStock.model.dto.CategoryResponseDTO;
import fr.doranco.GestionStock.model.dto.CategoryUpdateDTO;
import fr.doranco.GestionStock.model.entity.Category;
import fr.doranco.GestionStock.repository.ICategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service de gestion des catégories de produits.
 * Ce service fournit des méthodes pour créer, récupérer, mettre à jour et supprimer des catégories de produits.
 * Il utilise le repository ICategoryRepository pour interagir avec la base de données.
 * @since 2026-06-01
 * @author Siham HA
 * @version 1.0
 * @see ICategoryRepository
 */
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

        Optional<Category> existingCategory = categoryRepository.findByNameIncludingDeleted(request.name());
        if (existingCategory.isPresent()) {
            Category category = existingCategory.get();

            if (!category.isDeleted()) {
                log.warn("[CATEGORY] Creation failed: '{}' already exists and is active.", request.name());
                throw new IllegalArgumentException("Category with name " + request.name() + " already exists");
            } else {
                log.info("[CATEGORY] Restoring soft-deleted category: {}", request.name());
                category.setDeleted(false);
                category.setDescription(request.description());
                return mapToResponseDto(categoryRepository.save(category));
            }
        }

        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
        Category savedCategory = categoryRepository.save(category);

        log.info("[CATEGORY] Succès : Catégorie créée avec l'ID {}", savedCategory.getId());

        return mapToResponseDto(savedCategory);
    }

    /**
     * Récupère toutes les catégories de produits.
     * @return un ensemble de CategoryResponseDTO représentant toutes les catégories de produits.
     * @throws Exception si une erreur survient lors de la récupération des catégories.
     * @since 2026-06-01
     * @see CategoryResponseDTO
     * @see ICategoryRepository
     */
    @Override
    public Set<CategoryResponseDTO> getAllCategories() throws Exception {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toSet());
    }

    /**
     * Récupère une catégorie de produit par son ID.
     * @param id l'ID de la catégorie à récupérer.
     * @return un CategoryResponseDTO représentant la catégorie de produit.
     * @throws Exception si l'ID est invalide ou si aucune catégorie n'est trouvée avec cet ID.
     * @since 2026-06-01
     * @see CategoryResponseDTO
     * @see ICategoryRepository
     */
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

    /**
     * Met à jour une catégorie de produit existante.
     * @param id  l'ID de la catégorie à mettre à jour.
     * @param dto un CategoryUpdateDTO contenant les nouvelles informations de la catégorie.
     * @return un CategoryResponseDTO représentant la catégorie mise à jour.
     * @throws Exception si l'ID est invalide, si le DTO est nul, si aucune catégorie n'est trouvée avec cet ID, ou si le nom de la catégorie existe déjà.
     * @since 2026-06-01
     * @see CategoryUpdateDTO
     * @see CategoryResponseDTO
     * @see ICategoryRepository
     */
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

    /**
     * Supprime une catégorie de produit en la marquant comme supprimée (soft delete).
     * @param id l'ID de la catégorie à supprimer.
     * @throws Exception si l'ID est invalide ou si aucune catégorie n'est trouvée avec cet ID.
     * @since 2026-06-01
     * @see ICategoryRepository
     */
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

    /**
     * Mappe une entité Category vers un DTO de réponse CategoryResponseDTO.
     * @param category l'entité Category à mapper.
     * @return un CategoryResponseDTO contenant les informations de la catégorie.
     * @since 2026-06-01
     * @see Category
     * @see CategoryResponseDTO
     */
    private CategoryResponseDTO mapToResponseDto(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
