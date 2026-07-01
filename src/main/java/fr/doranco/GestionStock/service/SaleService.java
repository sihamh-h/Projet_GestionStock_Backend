package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.enums.MovementType;
import fr.doranco.GestionStock.enums.OrderStatus;
import fr.doranco.GestionStock.enums.SourceType;
import fr.doranco.GestionStock.model.dto.*;
import fr.doranco.GestionStock.model.entity.*;
import fr.doranco.GestionStock.repository.IProductRepository;
import fr.doranco.GestionStock.repository.ISaleRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SaleService implements ISaleService{

    private final ISaleRepository saleRepository;
    private final IProductRepository productRepository;
    private final MovementService movementService;
    private final IUserRepository userRepository;
    @Override
    public SaleResponseDTO createSale(SaleRequestDTO dto, User currentUser) throws Exception {
        if(dto == null){
            log.error("[SALE] Creation attempt with a null request");
            throw new IllegalArgumentException("Request is null");
        }
        if(dto.saleNumber() == null || dto.lines() == null || dto.saleNumber().isEmpty()) {
            log.error("[SALE] Creation attempt with invalid data: {}", dto);
            throw new IllegalArgumentException("Invalid sale data");
        }

        Sale sale = Sale.builder()
                .saleNumber(dto.saleNumber())
                .saleDate(LocalDate.now())
                .lines(new ArrayList<>())
                .build();
        float total = 0;
        for(SaleLineRequestDTO line : dto.lines()) {
            if(line.productId() == null || line.quantity() <= 0) {
                log.error("[SALE] Creation attempt with invalid line data: {}", line);
                throw new IllegalArgumentException("Invalid line data: productId must be provided and quantity must be greater than 0");
            }
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> {
                        log.warn("[SALE] Creation failed: No product found with ID {}", line.productId());
                        return new IllegalArgumentException("Product not found");
                    });
            if(product.getActualStock() < line.quantity()) {
                log.warn("[SALE] Creation failed: Insufficient stock for product ID {}. Requested: {}, Available: {}", line.productId(), line.quantity(), product.getActualStock());
                throw new IllegalStateException("Insufficient stock for product ID " + line.productId());
            }
            SaleLine saleLine = SaleLine.builder()
                    .product(product)
                    .quantity(line.quantity())
                    .unitPrice(product.getPrice())
                    .sale(sale)
                    .build();
            sale.getLines().add(saleLine);
            total += line.quantity() * product.getPrice().floatValue();
            movementService.recordMovement(
                    product, line.quantity(), MovementType.OUT,
                    SourceType.SALE
            );

        }
        sale.setTotalAmount(total);
        Sale savedSale = saleRepository.save(sale);
        log.info("[SALE] Sale created successfully with ID {}", savedSale.getId());
        return mapToResponse(savedSale);

    }

    @Override
    public List<SaleResponseDTO> getAll() throws Exception {
        return saleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

    @Override
    public SaleResponseDTO getById(Long id) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid sale ID: " + id);
        }
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale not found with ID: " + id));
        return mapToResponse(sale);
    }

    private SaleResponseDTO mapToResponse(Sale sale) {

        String createdByName = "—";
        if (sale.getCreatedBy() != null) {
            createdByName = userRepository.findById(sale.getCreatedBy())
                    .map(u -> u.getFirstname() + " " + u.getLastname())
                    .orElse("—");
        }
        return SaleResponseDTO.builder()
                .id(sale.getId())
                .saleNumber(sale.getSaleNumber())
                .saleDate(sale.getSaleDate())
                .totalAmount(sale.getTotalAmount())
                .createdByName(createdByName)
                .lines(sale.getLines().stream().map(line -> {
                    BigDecimal subTotal = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));

                    String productName = productRepository
                            .findByIdIncludingDeleted(line.getProduct().getId())
                            .map(p -> p.isDeleted() ? p.getName() + " (supprimé)" : p.getName())
                            .orElse("Produit introuvable");

                    return SaleLineResponseDTO.builder()
                            .productName(productName)
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .subTotal(subTotal)
                            .build();
                }).toList())
                .build();

    }
}
