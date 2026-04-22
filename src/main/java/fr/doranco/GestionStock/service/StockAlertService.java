package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.StockAlertDTO;
import fr.doranco.GestionStock.model.entity.StockAlert;
import fr.doranco.GestionStock.repository.IStockAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockAlertService implements IStockAlertService {

    private final IStockAlertRepository stockAlertRepository;
    @Override
    public List<StockAlertDTO> getActiveAlerts() throws Exception {
        return stockAlertRepository.findByResolvedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(alert -> StockAlertDTO.builder()
                        .id(alert.getId())
                        .productReference(alert.getProduct().getReference())
                        .productName(alert.getProduct().getName())
                        .message(alert.getMessage())
                        .severity(alert.getSeverity())
                        .createdAt(alert.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public void resolveAlert(Long id) throws Exception {
        if(id == null || id < 0) {
            log.error("[STOCK ALERT] Attempt to resolve alert with invalid ID: {}", id);
            throw new IllegalArgumentException("Invalid alert ID: " + id);
        }

        StockAlert alert = stockAlertRepository.findById(id).orElseThrow(() -> {
            log.error("[STOCK ALERT] Alert with ID {} not found for resolution", id);
            return new IllegalArgumentException("Alert with ID " + id + " not found");
        });
        alert.setResolvedAt(LocalDateTime.now());
        stockAlertRepository.save(alert);
        log.info("[ALERT] Alerte {} résolue manuellement", id);

    }
}
