package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.SaleRequestDTO;
import fr.doranco.GestionStock.model.entity.User;
import org.springframework.http.ResponseEntity;

public interface ISaleController {
    ResponseEntity<?> create(SaleRequestDTO dto, User currentUser) throws Exception;
}
