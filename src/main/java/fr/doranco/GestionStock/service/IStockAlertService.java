package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.StockAlertDTO;

import java.util.List;

public interface IStockAlertService {
    List<StockAlertDTO> getActiveAlerts() throws Exception;
    void resolveAlert(Long id) throws Exception;
}
