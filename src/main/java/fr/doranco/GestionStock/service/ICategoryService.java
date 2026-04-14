package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.CategoryRequestDTO;
import fr.doranco.GestionStock.model.dto.CategoryResponseDTO;
import fr.doranco.GestionStock.model.entity.Category;

public interface ICategoryService {
    CategoryResponseDTO createCategory(CategoryRequestDTO request) throws Exception;
}
