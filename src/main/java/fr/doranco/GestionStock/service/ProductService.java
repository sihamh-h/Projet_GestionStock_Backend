package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.ProductRequestDTO;
import fr.doranco.GestionStock.model.dto.ProductResponseDTO;
import fr.doranco.GestionStock.model.dto.ProductUpdateDTO;
import fr.doranco.GestionStock.model.entity.Category;
import fr.doranco.GestionStock.model.entity.Product;
import fr.doranco.GestionStock.repository.ICategoryRepository;
import fr.doranco.GestionStock.repository.IProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductService implements IProductService {
    private final ICategoryRepository categoryRepository;
    private final IProductRepository productRepository;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO request) throws Exception {
        if(request == null){
            log.error("[PRODUCT] Creation attempt with a null request");
            throw new IllegalArgumentException("Request is null");
        }
        if(request.reference() == null || request.name() == null || request.name().isBlank() || request.price() == null || request.price() < 0 || request.actualStock() == null || request.actualStock() < 0 || request.categoryId() == null || request.categoryId() < 0 || request.minStock() == null || request.minStock() < 0){
            log.error("[PRODUCT] Creation attempt with invalid data: name={}, price={}, actualStock={}, categoryId={}, minStock={}", request.name(), request.price(), request.actualStock(), request.categoryId(), request.minStock());
            throw new IllegalArgumentException("Invalid product data: name, price, actualStock, categoryId and minStock must be provided and valid");
        }
        if(productRepository.existsByReference(request.reference())){
            log.warn("[PRODUCT] Creation failed: A product with reference '{}' already exists in the database.", request.reference());
            throw new IllegalArgumentException("Product with reference " + request.reference() + " already exists");
        }
        Category category = categoryRepository.findById(request.categoryId()).orElseThrow(() -> {
            log.warn("[PRODUCT] Creation failed: No category found with ID {}", request.categoryId());
            return new IllegalArgumentException("Category with ID " + request.categoryId() + " not found");
        });
        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .actualStock(request.actualStock())
                .category(category)
                .reference(request.reference())
                .minStock(request.minStock())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .build();
        Product savedProduct = productRepository.save(product);
        log.info("[PRODUCT] Success: Product created with ID {}", savedProduct.getId());
        return mapToResponseDto(savedProduct);
    }

    @Override
    public Set<ProductResponseDTO> getAllProducts() throws  Exception {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toSet());
    }

    @Override
    public ProductResponseDTO getProductById(Long id) throws Exception {
        if(id == null || id < 0){
            log.error("[PRODUCT] Retrieval attempt with invalid ID: {}", id);
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
        Product product = productRepository.findById(id).orElseThrow(() -> {
            log.warn("[PRODUCT] Retrieval failed: No product found with ID {}", id);
            return new IllegalArgumentException("Product with ID " + id + " not found");
        });
        return mapToResponseDto(product);
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO dto) throws Exception {
        if(id == null || id < 0){
            log.error("[PRODUCT] Update attempt with invalid ID: {}", id);
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
        if(dto == null) {
            log.error("[PRODUCT] Update attempt with a null request");
            throw new IllegalArgumentException("Request is null");
        }
        Product existingProduct = productRepository.findById(id).orElseThrow(() -> {
            log.warn("[PRODUCT] Update failed: No product found with ID {}", id);
            return new IllegalArgumentException("Product with ID " + id + " not found");
        });
        if(dto.reference() != null && !dto.reference().equals(existingProduct.getReference())){
            if(productRepository.existsByReference(dto.reference())){
                log.warn("[PRODUCT] Update failed: A product with reference '{}' already exists in the database.", dto.reference());
                throw new IllegalArgumentException("Product with reference " + dto.reference() + " already exists");
            }
            existingProduct.setReference(dto.reference());
        }
        if(dto.categoryId() != null && !dto.categoryId().equals(existingProduct.getCategory().getId())){
            Category category = categoryRepository.findById(dto.categoryId()).orElseThrow(() -> {
                log.warn("[PRODUCT] Update failed: No category found with ID {}", dto.categoryId());
                return new IllegalArgumentException("Category with ID " + dto.categoryId() + " not found");
            });
            existingProduct.setCategory(category);
        }
        if (dto.name() != null) {
            existingProduct.setName(dto.name());
        }

        if (dto.description() != null) {
            existingProduct.setDescription(dto.description());
        }

        if (dto.price() != null) {
            existingProduct.setPrice(dto.price());
        }

        if (dto.actualStock() != null) {
            existingProduct.setActualStock(dto.actualStock());
        }

        if (dto.minStock() != null) {
            existingProduct.setMinStock(dto.minStock());
        }
        if(dto.imageUrl() != null) {
            existingProduct.setImageUrl(dto.imageUrl());
        }

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("[PRODUCT] Success: Product with ID {} updated", id);
        return mapToResponseDto(updatedProduct);

    }

    @Override
    public void deleteProduct(Long id) throws Exception {
        if(id == null || id < 0){
            log.error("[PRODUCT] Deletion attempt with invalid ID: {}", id);
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
        Product product = productRepository.findById(id).orElseThrow(() -> {
            log.warn("[PRODUCT] Deletion failed: No product found with ID {}", id);
            return new IllegalArgumentException("Product with ID " + id + " not found");
        });
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
        log.info("[PRODUCT] Success: Product with ID {} marked as deleted", id);
    }

    private ProductResponseDTO mapToResponseDto(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .actualStock(product.getActualStock())
                .categoryId(product.getCategory().getId())
                .reference(product.getReference())
                .minStock(product.getMinStock())
                .description(product.getDescription())
                .categoryName(product.getCategory().getName())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
