package fr.doranco.GestionStock.event;


import fr.doranco.GestionStock.enums.SeverityAlert;
import fr.doranco.GestionStock.model.dto.StockAlertDTO;
import fr.doranco.GestionStock.model.entity.StockAlert;
import fr.doranco.GestionStock.repository.IStockAlertRepository;
import fr.doranco.GestionStock.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.print.attribute.standard.Severity;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class StockAlertListener {
    private final IStockAlertRepository alertRepository;
    private final SseService sseService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLowStock(LowStockEvent event) {
        var product = event.getProduct();

        if (alertRepository.existsByProductAndResolvedAtIsNull(product)) return;

        log.warn("[ALERT SYSTEM] Generating alert for product: {}", product.getName());

        // Déterminer la sévérité
        SeverityAlert severity = SeverityAlert.MEDIUM;
        if (product.getActualStock() == 0) {
            severity = SeverityAlert.CRITICAL;
        } else if (product.getActualStock() > product.getMinStock()) {
            alertRepository.findByProductAndResolvedAtIsNull(product)
                    .ifPresent(alert -> {
                        alert.setResolvedAt(LocalDateTime.now());
                        alertRepository.save(alert);
                    });
            return;
        }


        String message = product.getActualStock() == 0
                ? "Rupture de stock : " + product.getName()
                : "Stock bas : " + product.getName()
                + " (" + product.getActualStock() + " / min " + product.getMinStock() + ")";

        // Créer et sauvegarder l'alerte
        StockAlert alert = StockAlert.builder()
                .product(product)
                .message(message)
                .severity(severity)
                .createdAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);

        // Envoyer l'alerte aux clients SSE
        StockAlertDTO alertDto = StockAlertDTO.builder()
                .id(alert.getId())
                .productName(product.getName())
                .productReference(product.getReference())
                .message(message)
                .severity(severity)
                .createdAt(alert.getCreatedAt())
                .build();
        sseService.sendAlert(alertDto);
    }
}
