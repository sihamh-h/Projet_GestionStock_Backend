package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.service.StockAlertService;
import fr.doranco.GestionStock.sse.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/rest/alerts")
@RequiredArgsConstructor
public class StockAlertController implements  IStockAlertController {

    private final StockAlertService alertService;
    private final SseService sseService;

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('ALERT_READ')")
    public ResponseEntity<?> getActiveAlerts() throws Exception {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @Override
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('ALERT_READ')")
    public ResponseEntity<?> stream() throws Exception {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        sseService.createEmitter(emitter);
        return ResponseEntity.ok(emitter);
    }

    @Override
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('ALERT_RESOLVE')")
    public ResponseEntity<?> resolveAlert(@PathVariable  Long id) throws Exception {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid alert ID");
        }
        alertService.resolveAlert(id);
        return ResponseEntity.noContent().build();
    }
}
