package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.SupplierRequestDTO;
import fr.doranco.GestionStock.model.dto.SupplierResponseDTO;

import java.util.List;

public interface ISupplierService {
    SupplierResponseDTO create(SupplierRequestDTO request) throws Exception;
    List<SupplierResponseDTO> getAll() throws Exception;
    SupplierResponseDTO getById(Long id) throws Exception;
    SupplierResponseDTO update(Long id, SupplierRequestDTO request) throws Exception;
    void delete(Long id) throws Exception;
}
