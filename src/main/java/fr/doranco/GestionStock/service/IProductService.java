package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.ProductRequestDTO;
import fr.doranco.GestionStock.model.dto.ProductResponseDTO;
import fr.doranco.GestionStock.model.dto.ProductUpdateDTO;

import java.util.Set;


public interface IProductService {
    ProductResponseDTO createProduct(ProductRequestDTO request) throws Exception;
    Set<ProductResponseDTO> getAllProducts() throws Exception;
    ProductResponseDTO getProductById(Long id) throws Exception;
    ProductResponseDTO updateProduct(Long id, ProductUpdateDTO dto) throws Exception;
    void deleteProduct(Long id) throws Exception;

}
