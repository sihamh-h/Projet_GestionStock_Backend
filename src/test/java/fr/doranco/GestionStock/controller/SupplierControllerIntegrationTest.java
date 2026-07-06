package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.SupplierResponseDTO;
import fr.doranco.GestionStock.service.ISupplierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration : montent le contexte Spring complet (sécurité incluse)
 * et mockent uniquement la couche service. Vérifie le routing et les règles @PreAuthorize.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SupplierControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ISupplierService supplierService;

    @Test
    @WithMockUser(authorities = "SUPPLIER_CREATE")
    public void testCreateSupplierSuccessfully() throws Exception {
        SupplierResponseDTO response = SupplierResponseDTO.builder()
                .id(1L)
                .number("SUP-001")
                .name("Fournisseur Test")
                .build();

        when(supplierService.create(any())).thenReturn(response);

        String body = """
                {
                  "number": "SUP-001",
                  "name": "Fournisseur Test",
                  "email": "contact@test.fr",
                  "phone": "0601020304"
                }
                """;

        mockMvc.perform(post("/api/rest/supplier/create")
                        .with(csrf())
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());
    }


    @Test
    @WithMockUser(authorities = "SUPPLIER_READ")
    public void testGetAllSuccessfully() throws Exception {
        SupplierResponseDTO response = SupplierResponseDTO.builder()
                .id(1L)
                .number("SUP-001")
                .name("Fournisseur Test")
                .build();

        when(supplierService.getAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/rest/supplier"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "SUPPLIER_READ")
    public void testGetByIdSuccessfully() throws Exception {
        SupplierResponseDTO response = SupplierResponseDTO.builder()
                .id(1L)
                .number("SUP-001")
                .name("Fournisseur Test")
                .build();

        when(supplierService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/rest/supplier/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "SUPPLIER_DELETE")
    public void testDeleteSupplierSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/rest/supplier/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

}