package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.SupplierRequestDTO;
import fr.doranco.GestionStock.model.dto.SupplierResponseDTO;
import fr.doranco.GestionStock.model.entity.Supplier;
import fr.doranco.GestionStock.repository.ISupplierRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de SupplierService (Mockito).
 * SupplierRequestDTO : number, name, companyName, website, siret, email, phone, address
 */
@ExtendWith(MockitoExtension.class)
public class SupplierServiceTest {

    @Mock
    private ISupplierRepository supplierRepository;

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    public void testCreateSupplierSuccessfully() throws Exception {
        SupplierRequestDTO request = new SupplierRequestDTO(
                "SUP-001", "Fournisseur Test", "Test SARL", "https://test.fr",
                "12345678901234", "contact@test.fr", "0601020304", "1 rue de Paris"
        );

        Supplier savedSupplier = Supplier.builder()
                .id(1L)
                .number("SUP-001")
                .name("Fournisseur Test")
                .email("contact@test.fr")
                .phoneNumber("0601020304")
                .build();

        when(supplierRepository.findByNumberOrSiretIncludingDeleted("SUP-001", "12345678901234"))
                .thenReturn(Optional.empty());
        when(supplierRepository.save(any(Supplier.class))).thenReturn(savedSupplier);

        SupplierResponseDTO result = supplierService.create(request);

        assertEquals(1L, result.id());
        assertEquals("SUP-001", result.number());
        assertEquals("Fournisseur Test", result.name());
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    public void testCreateSupplierWithExistingNumber() {
        SupplierRequestDTO request = new SupplierRequestDTO(
                "SUP-002", "Fournisseur Test", null, null,
                null, "contact@test.fr", "0601020304", null
        );

        Supplier existingSupplier = Supplier.builder()
                .id(5L)
                .number("SUP-002")
                .build();

        when(supplierRepository.findByNumberOrSiretIncludingDeleted("SUP-002", null))
                .thenReturn(Optional.of(existingSupplier));

        assertThrows(IllegalArgumentException.class, () -> supplierService.create(request));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    public void testCreateSupplierWithNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> supplierService.create(null));
    }

    @Test
    public void testGetByIdSuccessfully() throws Exception {
        Supplier supplier = Supplier.builder()
                .id(3L)
                .number("SUP-010")
                .name("Fournisseur A")
                .build();

        when(supplierRepository.findByIdAndDeletedFalse(3L)).thenReturn(Optional.of(supplier));

        SupplierResponseDTO result = supplierService.getById(3L);

        assertEquals(3L, result.id());
        assertEquals("SUP-010", result.number());
    }

    @Test
    public void testGetByIdNotFound() {
        when(supplierRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> supplierService.getById(999L));
    }

    @Test
    public void testUpdateSupplierSuccessfully() throws Exception {
        Supplier existing = Supplier.builder()
                .id(4L)
                .number("SUP-020")
                .name("Ancien nom")
                .build();

        SupplierRequestDTO request = new SupplierRequestDTO(
                "SUP-020", "Nouveau nom", null, null,
                null, "contact@test.fr", "0601020304", null
        );

        when(supplierRepository.findByIdAndDeletedFalse(4L)).thenReturn(Optional.of(existing));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierResponseDTO result = supplierService.update(4L, request);

        assertEquals("Nouveau nom", result.name());
    }

    @Test
    public void testDeleteSupplierSuccessfully() throws Exception {
        Supplier supplier = Supplier.builder()
                .id(6L)
                .number("SUP-030")
                .build();

        when(supplierRepository.findById(6L)).thenReturn(Optional.of(supplier));

        supplierService.delete(6L);

        assertTrue(supplier.isDeleted());
        verify(supplierRepository).save(supplier);
    }

    @Test
    public void testDeleteSupplierNotFound() {
        when(supplierRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> supplierService.delete(404L));
    }

    @Test
    public void testGetAllSuccessfully() throws Exception {
        Supplier s1 = Supplier.builder().id(1L).number("SUP-A").build();
        Supplier s2 = Supplier.builder().id(2L).number("SUP-B").build();

        when(supplierRepository.findAllByDeletedFalse()).thenReturn(List.of(s1, s2));

        List<SupplierResponseDTO> result = supplierService.getAll();

        assertEquals(2, result.size());
    }
}