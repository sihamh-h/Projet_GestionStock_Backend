package fr.doranco.GestionStock.mongo.service;

import fr.doranco.GestionStock.mongo.dto.ActivityLogRequestDTO;
import fr.doranco.GestionStock.mongo.dto.ActivityLogResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IActivityLogService {
    void log(ActivityLogRequestDTO request) throws Exception;
    List<ActivityLogResponseDTO> getLogsByAction(String action) throws Exception;
    List<ActivityLogResponseDTO> getLogsByUser(Long userId) throws Exception;
    List<ActivityLogResponseDTO> getFailedLogs() throws Exception;
    Page<ActivityLogResponseDTO> getLogs(String action, Long userId, Boolean success, int page, int size) throws Exception;
}
