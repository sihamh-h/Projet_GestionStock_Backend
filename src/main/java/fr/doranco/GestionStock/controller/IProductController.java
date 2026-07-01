package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.CategoryRequestDTO;
import fr.doranco.GestionStock.model.dto.ProductRequestDTO;
import fr.doranco.GestionStock.model.dto.ProductUpdateDTO;
import org.springframework.http.ResponseEntity;

public interface IProductController {
    ResponseEntity<?> create(ProductRequestDTO request) throws Exception;
    ResponseEntity<?> getAllProducts() throws Exception;
    ResponseEntity<?> getProductById (Long id) throws Exception;
    ResponseEntity<?> updateProduct (Long id, ProductUpdateDTO dto) throws Exception;
    ResponseEntity<?> deleteProduct( Long id) throws Exception;
    ResponseEntity<?> restoreProduct(Long id) throws Exception ;
    ResponseEntity<?> getDeletedProducts() throws Exception ;
}
