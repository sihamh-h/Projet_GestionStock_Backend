package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.CategoryRequestDTO;
import fr.doranco.GestionStock.model.dto.ProductRequestDTO;
import org.springframework.http.ResponseEntity;

public interface IProductController {
    ResponseEntity<?> create(ProductRequestDTO request) throws Exception;
}
