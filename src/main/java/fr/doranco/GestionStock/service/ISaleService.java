package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.SaleRequestDTO;
import fr.doranco.GestionStock.model.dto.SaleResponseDTO;
import fr.doranco.GestionStock.model.entity.User;

public interface ISaleService {
    SaleResponseDTO createSale(SaleRequestDTO dto, User currentUser) throws Exception;
}
