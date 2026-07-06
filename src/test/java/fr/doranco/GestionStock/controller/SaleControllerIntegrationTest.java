package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.SaleResponseDTO;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.service.ISaleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class SaleControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ISaleService saleService;

    @Test
    @WithMockUser(authorities = "SALE_CREATE")
    public void testCreateSaleSuccessfully() throws Exception {
        SaleResponseDTO response = SaleResponseDTO.builder()
                .id(1L)
                .saleNumber("V-001")
                .saleDate(LocalDate.now())
                .totalAmount(20f)
                .build();

        when(saleService.createSale(any(), any())).thenReturn(response);

        String body = """
                {
                  "saleNumber": "V-001",
                  "lines": [
                    { "productId": 1, "quantity": 2, "unitPrice": 10.00 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/rest/sales/create")
                        .with(csrf())
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "SALE_READ")
    public void testGetAllSuccessfully() throws Exception {
        SaleResponseDTO response = SaleResponseDTO.builder()
                .id(1L)
                .saleNumber("V-010")
                .saleDate(LocalDate.now())
                .totalAmount(15f)
                .build();

        when(saleService.getAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/rest/sales"))
                .andExpect(status().isOk());
    }

}
