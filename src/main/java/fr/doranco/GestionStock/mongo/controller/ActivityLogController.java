package fr.doranco.GestionStock.mongo.controller;

import fr.doranco.GestionStock.mongo.service.IActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rest/logs")
@CrossOrigin(origins = "http://localhost:8080")
@RequiredArgsConstructor
@Slf4j
public class ActivityLogController implements  IActivityLogController {

    private final IActivityLogService activityLogService;

    @Override
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('LOG_READ')")
    public ResponseEntity<?> getLogsByUser(@PathVariable Long userId) throws Exception {
        if(userId == null) {
            return ResponseEntity.badRequest().body("User ID must not be null");
        }
        return ResponseEntity.ok(activityLogService.getLogsByUser(userId));
    }

    @Override
    @GetMapping("/action/{action}")
    @PreAuthorize("hasAuthority('LOG_READ')")
    public ResponseEntity<?> getLogsByAction(@PathVariable String action) throws Exception {
        if(action == null || action.isEmpty()) {
            return ResponseEntity.badRequest().body("Action must not be null or empty");
        }
        return ResponseEntity.ok(activityLogService.getLogsByAction(action));
    }

    @Override
    @GetMapping("/failed")
    @PreAuthorize("hasAuthority('LOG_READ')")
    public ResponseEntity<?> getFailedLogs() throws Exception {
        return ResponseEntity.ok(activityLogService.getFailedLogs());
    }
}
