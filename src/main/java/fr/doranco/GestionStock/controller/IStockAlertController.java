package fr.doranco.GestionStock.controller;

import org.springframework.http.ResponseEntity;

public interface IStockAlertController {
    ResponseEntity<?> getActiveAlerts() throws Exception;
    ResponseEntity<?> stream() throws Exception;
    ResponseEntity<?> resolveAlert(Long id) throws Exception;
}
