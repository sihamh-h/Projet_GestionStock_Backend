package fr.doranco.GestionStock.sse;


import fr.doranco.GestionStock.model.dto.StockAlertDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class SseService {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void createEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        log.info("[SSE] New emitter created. Total emitters: {}", emitters.size());
    }

    public void sendAlert(StockAlertDTO alert) {
        log.info("[SSE] Pushing alert to {} emitters: {}", emitters.size(), alert);
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("stock-alert")
                        .data(alert));
            } catch (IOException e) {
                log.warn("[SSE] Failed to send alert to an emitter, removing it. Error: {}", e.getMessage());
                emitters.remove(emitter);
            }
        });
    }
}
