package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.ProductRequestDTO;
import fr.doranco.GestionStock.model.dto.ProductResponseDTO;
import fr.doranco.GestionStock.model.dto.ProductUpdateDTO;
import fr.doranco.GestionStock.model.entity.Category;
import fr.doranco.GestionStock.model.entity.Product;
import fr.doranco.GestionStock.mongo.service.IActivityLogService;
import fr.doranco.GestionStock.repository.ICategoryRepository;
import fr.doranco.GestionStock.repository.IProductRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import fr.doranco.GestionStock.utils.RequestUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ICategoryRepository categoryRepository;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IActivityLogService activityLogService;

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    public void testCreateProductSuccessfully() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Électronique");

        ProductRequestDTO request = new ProductRequestDTO(
                "REF-001", "Clavier mécanique", "Clavier RGB", BigDecimal.valueOf(59.99),
                50, 5, "http://image.url/clavier.png", 1L
        );

        Product savedProduct = Product.builder()
                .id(100L)
                .reference("REF-001")
                .name("Clavier mécanique")
                .category(category)
                .build();

        when(productRepository.findByReferenceIncludingDeleted("REF-001")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        try (MockedStatic<RequestUtils> requestUtils = mockStatic(RequestUtils.class)) {
            requestUtils.when(RequestUtils::getCurrentUserId).thenReturn(1L);
            requestUtils.when(RequestUtils::getCurrentUserEmail).thenReturn("admin@stockiflow.com");

            ProductResponseDTO result = productService.createProduct(request);

            assertEquals(100L, result.id());
            assertEquals("REF-001", result.reference());
        }

        verify(productRepository).save(any(Product.class));
        verify(activityLogService).log(any());
    }

    @Test
    public void testCreateProductWithExistingReference() {
        ProductRequestDTO request = new ProductRequestDTO(
                "REF-003", "Écran 24 pouces", "Écran Full HD", BigDecimal.valueOf(149.99),
                10, 2, null, 1L
        );

        Product existingProduct = Product.builder()
                .id(10L)
                .reference("REF-003")
                .build();
        existingProduct.setDeleted(false);

        when(productRepository.findByReferenceIncludingDeleted("REF-003")).thenReturn(Optional.of(existingProduct));

        try (MockedStatic<RequestUtils> requestUtils = mockStatic(RequestUtils.class)) {
            requestUtils.when(RequestUtils::getCurrentUserId).thenReturn(1L);
            requestUtils.when(RequestUtils::getCurrentUserEmail).thenReturn("admin@stockiflow.com");

            assertThrows(IllegalArgumentException.class, () -> productService.createProduct(request));
        }

        verify(productRepository, never()).save(any());
    }

    @Test
    public void testCreateProductWithBlankName() {
        ProductRequestDTO request = new ProductRequestDTO(
                "REF-004", "", "desc", BigDecimal.TEN, 10, 1, null, 1L
        );

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(request));
    }

    @Test
    public void testGetProductByIdSuccessfully() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Électronique");

        Product product = Product.builder()
                .id(5L)
                .reference("REF-010")
                .name("Test")
                .category(category)
                .build();

        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        ProductResponseDTO result = productService.getProductById(5L);

        assertEquals(5L, result.id());
        assertEquals("REF-010", result.reference());
    }

    @Test
    public void testUpdateProductSuccessfully() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Électronique");

        Product existing = Product.builder()
                .id(7L)
                .reference("REF-020")
                .name("Ancien nom")
                .price(BigDecimal.TEN)
                .actualStock(10)
                .minStock(2)
                .category(category)
                .build();

        ProductUpdateDTO dto = new ProductUpdateDTO(
                null, "Nouveau nom", null, null, null, null, null, null
        );

        when(productRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);

        try (MockedStatic<RequestUtils> requestUtils = mockStatic(RequestUtils.class)) {
            requestUtils.when(RequestUtils::getCurrentUserId).thenReturn(1L);
            requestUtils.when(RequestUtils::getCurrentUserEmail).thenReturn("admin@stockiflow.com");

            ProductResponseDTO result = productService.updateProduct(7L, dto);

            assertEquals("Nouveau nom", result.name());
        }
    }

    @Test
    public void testDeleteProductSuccessfully() throws Exception {
        Product product = Product.builder()
                .id(3L)
                .reference("REF-040")
                .build();
        product.setDeleted(false);

        when(productRepository.findById(3L)).thenReturn(Optional.of(product));

        try (MockedStatic<RequestUtils> requestUtils = mockStatic(RequestUtils.class)) {
            requestUtils.when(RequestUtils::getCurrentUserId).thenReturn(1L);
            requestUtils.when(RequestUtils::getCurrentUserEmail).thenReturn("admin@stockiflow.com");

            productService.deleteProduct(3L);
        }

        assertTrue(product.isDeleted());
        verify(productRepository).save(product);
    }

    @Test
    public void testDeleteProductWhenProductNotFound() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        try (MockedStatic<RequestUtils> requestUtils = mockStatic(RequestUtils.class)) {
            requestUtils.when(RequestUtils::getCurrentUserId).thenReturn(1L);
            requestUtils.when(RequestUtils::getCurrentUserEmail).thenReturn("admin@stockiflow.com");

            assertThrows(IllegalArgumentException.class, () -> productService.deleteProduct(404L));
        }
    }

}
