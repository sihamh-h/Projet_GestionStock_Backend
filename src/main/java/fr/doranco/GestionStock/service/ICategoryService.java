package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.*;
import fr.doranco.GestionStock.model.entity.Category;

import java.util.Set;

public interface ICategoryService {
    CategoryResponseDTO createCategory(CategoryRequestDTO request) throws Exception;
    Set<CategoryResponseDTO> getAllCategories() throws Exception;
    CategoryResponseDTO getCategoryById(Long id) throws Exception;
    CategoryResponseDTO updateCategory(Long id, CategoryUpdateDTO dto) throws Exception;
    void deleteCategory(Long id) throws Exception;
}
