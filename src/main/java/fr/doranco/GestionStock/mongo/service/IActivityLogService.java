package fr.doranco.GestionStock.mongo.service;

import fr.doranco.GestionStock.mongo.dto.ActivityLogRequestDTO;
import fr.doranco.GestionStock.mongo.dto.ActivityLogResponseDTO;

import java.util.List;

public interface IActivityLogService {
    void log(ActivityLogRequestDTO request) throws Exception;
    List<ActivityLogResponseDTO> getLogsByAction(String action) throws Exception;
    List<ActivityLogResponseDTO> getLogsByUser(Long userId) throws Exception;
    List<ActivityLogResponseDTO> getFailedLogs() throws Exception;
}
