package fr.doranco.GestionStock.mongo.controller;

import org.springframework.http.ResponseEntity;

public interface IActivityLogController {

    ResponseEntity<?> getLogsByUser(Long userId) throws Exception;
    ResponseEntity<?> getLogsByAction(String action) throws Exception;
    ResponseEntity<?> getFailedLogs() throws Exception;
}
