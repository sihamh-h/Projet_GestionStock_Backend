package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.CategoryRequestDTO;
import org.springframework.http.ResponseEntity;

public interface ICategoryController {
    ResponseEntity<?> create(CategoryRequestDTO request) throws Exception;
}
