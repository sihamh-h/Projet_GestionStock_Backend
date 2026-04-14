package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.ProductRequestDTO;
import fr.doranco.GestionStock.model.dto.ProductResponseDTO;


public interface IProductService {
    ProductResponseDTO createProduct(ProductRequestDTO request) throws Exception;
}
