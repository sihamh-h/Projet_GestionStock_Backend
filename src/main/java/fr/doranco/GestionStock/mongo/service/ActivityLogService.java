package fr.doranco.GestionStock.mongo.service;


import fr.doranco.GestionStock.mongo.document.ActivityLog;
import fr.doranco.GestionStock.mongo.dto.ActivityLogRequestDTO;
import fr.doranco.GestionStock.mongo.dto.ActivityLogResponseDTO;
import fr.doranco.GestionStock.mongo.repository.IActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService implements IActivityLogService {
   private final IActivityLogRepository activityLogRepository;
   private final MongoTemplate mongoTemplate;

    @Override
    @Async
    public void log(ActivityLogRequestDTO request) throws Exception {
        if(request == null) {
            throw new IllegalArgumentException("ActivityLogRequestDTO must not be null");
        }
        try {
            ActivityLog activityLog = ActivityLog.builder()
                    .userId(request.userId())
                    .userEmail(request.userEmail())
                    .action(request.action())
                    .description(request.description())
                    .timestamp(LocalDateTime.now(ZoneId.of("Europe/Zagreb")))
                    .success(request.success())
                    .build();

            activityLogRepository.save(activityLog);
            log.info("[ACTIVITY] {} - {} - {}", request.userEmail(), request.action(), request.description());

        } catch (Exception e) {
            log.error("[ACTIVITY] Failed to log activity for user {}: {}", request.userEmail(), e.getMessage());
            throw new Exception("Failed to log activity", e);
        }

    }

    @Override
    public List<ActivityLogResponseDTO> getLogsByAction(String action) throws Exception {
        if(action == null || action.isEmpty()) {
            log.warn("[ACTIVITY] Invalid action parameter: {}", action);
            throw new IllegalArgumentException("Action must not be null or empty");
        }

        List<ActivityLog> logs = activityLogRepository.findByAction(action);
        if(logs.isEmpty()) {
            log.warn("[ACTIVITY] No logs found for action: {}", action);
            throw new Exception("No logs found for action: " + action);
        }

        return mapToActivityLogResponseDTO(logs);

    }

    @Override
    public List<ActivityLogResponseDTO> getLogsByUser(Long userId) throws Exception {
        if(userId == null) {
                log.warn("[ACTIVITY] Invalid user ID parameter: {}", userId);
            throw new IllegalArgumentException("User ID must not be null");
        }
        List<ActivityLog> logs = activityLogRepository.findByUserId(userId);
        if(logs.isEmpty()) {
                log.warn("[ACTIVITY] No logs found for user ID: {}", userId);
            throw new Exception("No logs found for user ID: " + userId);
        }

        return mapToActivityLogResponseDTO(logs);
    }

    @Override
    public List<ActivityLogResponseDTO> getFailedLogs() throws Exception {
        List<ActivityLog> logs = activityLogRepository.findBySuccessFalse();
        if(logs.isEmpty()) {
            log.warn("[ACTIVITY] No failed logs found");
            throw new Exception("No failed logs found");
        }

        return mapToActivityLogResponseDTO(logs);
    }

    @Override
    public Page<ActivityLogResponseDTO> getLogs(String action, Long userId, Boolean success, int page, int size) throws Exception {
        Query query = new Query();

        if (action != null && !action.isEmpty()) {
            query.addCriteria(Criteria.where("action").is(action));
        }
        if (userId != null) {
            query.addCriteria(Criteria.where("userId").is(userId));
        }
        if (success != null) {
            query.addCriteria(Criteria.where("success").is(success));
        }

        // Total count
        long total = mongoTemplate.count(query, ActivityLog.class);

        // Pagination
        query.with(org.springframework.data.domain.PageRequest.of(page, size));
        query.with(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "timestamp"
        ));

        List<ActivityLog> logs = mongoTemplate.find(query, ActivityLog.class);

        List<ActivityLogResponseDTO> dtos = mapToActivityLogResponseDTO(logs);

        return new org.springframework.data.domain.PageImpl<>(dtos,
                org.springframework.data.domain.PageRequest.of(page, size), total);
    }

    List<ActivityLogResponseDTO> mapToActivityLogResponseDTO(List<ActivityLog> logs) {
        return logs.stream()
                .map(log -> ActivityLogResponseDTO.builder()
                        .id(log.getId())
                        .userId(log.getUserId())
                        .userEmail(log.getUserEmail())
                        .action(log.getAction())
                        .description(log.getDescription())
                        .timestamp(log.getTimestamp())
                        .success(log.isSuccess())
                        .build())
                .toList();
    }
}
