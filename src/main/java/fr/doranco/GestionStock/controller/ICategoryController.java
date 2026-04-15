package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.CategoryRequestDTO;
import fr.doranco.GestionStock.model.dto.CategoryUpdateDTO;
import fr.doranco.GestionStock.model.dto.ProductUpdateDTO;
import org.springframework.http.ResponseEntity;

public interface ICategoryController {
    ResponseEntity<?> create(CategoryRequestDTO request) throws Exception;
    ResponseEntity<?> getAllCategories() throws Exception;
    ResponseEntity<?> getCategoryById (Long id) throws Exception;
    ResponseEntity<?> updateCategory (Long id, CategoryUpdateDTO dto) throws Exception;
    ResponseEntity<?> deleteCategory( Long id) throws Exception;
}
