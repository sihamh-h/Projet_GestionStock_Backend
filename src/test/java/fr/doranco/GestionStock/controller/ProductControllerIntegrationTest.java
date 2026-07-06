package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.ProductResponseDTO;
import fr.doranco.GestionStock.service.IProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IProductService productService;


    @Test
    @WithMockUser(authorities = "PRODUCT_CREATE")
    public void testCreateProductSuccessfully() throws Exception {
        ProductResponseDTO response = ProductResponseDTO.builder()
                .id(1L)
                .reference("REF-001")
                .name("Clavier mécanique")
                .price(BigDecimal.valueOf(59.99))
                .build();

        when(productService.createProduct(any())).thenReturn(response);

        String body = """
                {
                  "reference": "REF-001",
                  "name": "Clavier mécanique",
                  "price": 59.99,
                  "actualStock": 50,
                  "minStock": 5,
                  "categoryId": 1
                }
                """;

        mockMvc.perform(post("/api/rest/product/create")
                        .with(csrf())
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "PRODUCT_READ")
    public void testGetAllProductsSuccessfully() throws Exception {
        ProductResponseDTO response = ProductResponseDTO.builder()
                .id(1L)
                .reference("REF-001")
                .name("Clavier mécanique")
                .build();

        when(productService.getAllProducts()).thenReturn(Set.of(response));

        mockMvc.perform(get("/api/rest/product/all"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(authorities = "PRODUCT_READ")
    public void testGetProductByIdSuccessfully() throws Exception {
        ProductResponseDTO response = ProductResponseDTO.builder()
                .id(1L)
                .reference("REF-001")
                .name("Clavier mécanique")
                .build();

        when(productService.getProductById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/rest/product/1"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(authorities = "PRODUCT_DELETE")
    public void testDeleteProductSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/rest/product/1").with(csrf()))
                .andExpect(status().isNoContent());
    }



}
