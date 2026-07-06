package fr.doranco.GestionStock.service;


import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.doranco.GestionStock.enums.MovementType;
import fr.doranco.GestionStock.enums.SourceType;
import fr.doranco.GestionStock.model.dto.SaleLineRequestDTO;
import fr.doranco.GestionStock.model.dto.SaleRequestDTO;
import fr.doranco.GestionStock.model.dto.SaleResponseDTO;
import fr.doranco.GestionStock.model.entity.Product;
import fr.doranco.GestionStock.model.entity.Sale;
import fr.doranco.GestionStock.repository.IProductRepository;
import fr.doranco.GestionStock.repository.ISaleRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SaleServiceTest {

    @Mock
    private ISaleRepository saleRepository;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private MovementService movementService;

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private SaleService saleService;

    @Test
    public void testCreateSaleSuccessfully() throws Exception {
        Product product = Product.builder()
                .id(1L)
                .name("Clavier mécanique")
                .price(BigDecimal.valueOf(10.00))
                .actualStock(5)
                .build();

        SaleLineRequestDTO line = new SaleLineRequestDTO(1L, 2, BigDecimal.valueOf(10.00));
        SaleRequestDTO request = new SaleRequestDTO("V-001", List.of(line));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findByIdIncludingDeleted(1L)).thenReturn(Optional.of(product));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            sale.setId(100L);
            return sale;
        });

        SaleResponseDTO result = saleService.createSale(request, null);

        assertEquals(100L, result.id());
        assertEquals("V-001", result.saleNumber());
        assertEquals(20.0f, result.totalAmount());
        verify(saleRepository).save(any(Sale.class));
        verify(movementService).recordMovement(product, 2, MovementType.OUT, SourceType.SALE);
    }

    @Test
    public void testCreateSaleWithInvalidLineQuantity() {
        SaleLineRequestDTO line = new SaleLineRequestDTO(1L, 0, BigDecimal.valueOf(10.00));
        SaleRequestDTO request = new SaleRequestDTO("V-002", List.of(line));

        assertThrows(IllegalArgumentException.class, () -> saleService.createSale(request, null));
    }

    @Test
    public void testCreateSaleWithInsufficientStock() {
        Product product = Product.builder()
                .id(1L)
                .name("Clavier mécanique")
                .price(BigDecimal.valueOf(10.00))
                .actualStock(1)
                .build();

        SaleLineRequestDTO line = new SaleLineRequestDTO(1L, 5, BigDecimal.valueOf(10.00));
        SaleRequestDTO request = new SaleRequestDTO("V-003", List.of(line));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(IllegalStateException.class, () -> saleService.createSale(request, null));

        verify(saleRepository, never()).save(any());
    }

    @Test
    public void testGetByIdSuccessfully() throws Exception {
        Sale sale = Sale.builder()
                .id(1L)
                .saleNumber("V-010")
                .saleDate(LocalDate.now())
                .totalAmount(20f)
                .lines(new ArrayList<>())
                .build();

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        SaleResponseDTO result = saleService.getById(1L);

        assertEquals(1L, result.id());
        assertEquals("V-010", result.saleNumber());
    }

    @Test
    public void testGetByIdWithInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> saleService.getById(-1L));
    }

    @Test
    public void testGetByIdNotFound() {
        when(saleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> saleService.getById(999L));
    }

    @Test
    public void testGetAllSuccessfully() throws Exception {
        Sale sale = Sale.builder()
                .id(1L)
                .saleNumber("V-020")
                .saleDate(LocalDate.now())
                .totalAmount(15f)
                .lines(new ArrayList<>())
                .build();

        when(saleRepository.findAll()).thenReturn(List.of(sale));

        List<SaleResponseDTO> result = saleService.getAll();

        assertEquals(1, result.size());
    }
}
