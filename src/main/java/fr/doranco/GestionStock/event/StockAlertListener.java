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

        // Stock OK → résoudre l'alerte existante si elle existe
        if (product.getActualStock() > product.getMinStock()) {
            alertRepository.findByProductAndResolvedAtIsNull(product)
                    .ifPresent(alert -> {
                        alert.setResolvedAt(LocalDateTime.now());
                        alertRepository.save(alert);
                    });
            return;
        }

        SeverityAlert severity;
        int stock = product.getActualStock();
        int min   = product.getMinStock();

        if (stock == 0) {
            severity = SeverityAlert.CRITICAL;          // rupture totale
        } else if (stock <= min * 0.25) {
            severity = SeverityAlert.HIGH;              // stock < 25% du min
        } else if (stock <= min * 0.5) {
            severity = SeverityAlert.MEDIUM;            // stock < 50% du min
        } else {
            severity = SeverityAlert.LOW;               // stock < min mais > 50%
        }
        System.out.println(" alert produit"+ product.getName());
        System.out.println("severité "+ severity);

        // Alerte existante → mettre à jour la sévérité si elle a changé
        alertRepository.findByProductAndResolvedAtIsNull(product)
                .ifPresentOrElse(
                        existingAlert -> {
                            if (existingAlert.getSeverity() != severity) {
                                existingAlert.setSeverity(severity);
                                existingAlert.setMessage(
                                        product.getActualStock() == 0
                                                ? "Rupture de stock : " + product.getName()
                                                : "Stock bas : " + product.getName()
                                                + " (" + product.getActualStock() + " / min " + product.getMinStock() + ")"
                                );
                                alertRepository.save(existingAlert);
                                // Notifie via SSE
                                sseService.sendAlert(StockAlertDTO.builder()
                                        .id(existingAlert.getId())
                                        .productName(product.getName())
                                        .productReference(product.getReference())
                                        .message(existingAlert.getMessage())
                                        .severity(severity)
                                        .createdAt(existingAlert.getCreatedAt())
                                        .build());
                            }
                        },
                        () -> {
                            // Pas d'alerte existante → créer une nouvelle
                            String message = product.getActualStock() == 0
                                    ? "Rupture de stock : " + product.getName()
                                    : "Stock bas : " + product.getName()
                                    + " (" + product.getActualStock() + " / min " + product.getMinStock() + ")";

                            StockAlert alert = StockAlert.builder()
                                    .product(product)
                                    .message(message)
                                    .severity(severity)
                                    .createdAt(LocalDateTime.now())
                                    .build();
                            alertRepository.save(alert);

                            sseService.sendAlert(StockAlertDTO.builder()
                                    .id(alert.getId())
                                    .productName(product.getName())
                                    .productReference(product.getReference())
                                    .message(message)
                                    .severity(severity)
                                    .createdAt(alert.getCreatedAt())
                                    .build());
                        }
                );
    }
}
