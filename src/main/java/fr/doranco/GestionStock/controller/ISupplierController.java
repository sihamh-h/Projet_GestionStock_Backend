package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.SupplierRequestDTO;
import org.springframework.http.ResponseEntity;

public interface ISupplierController {
    ResponseEntity<?> create(SupplierRequestDTO request) throws Exception;
    ResponseEntity<?> getAll() throws Exception;
    ResponseEntity<?> getById(Long id) throws Exception;
    ResponseEntity<?> update(Long id, SupplierRequestDTO request) throws Exception;
    ResponseEntity<?> delete(Long id) throws Exception;
}
