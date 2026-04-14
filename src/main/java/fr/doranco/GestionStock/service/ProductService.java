package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.ProductRequestDTO;
import fr.doranco.GestionStock.model.dto.ProductResponseDTO;
import fr.doranco.GestionStock.model.entity.Category;
import fr.doranco.GestionStock.model.entity.Product;
import fr.doranco.GestionStock.repository.ICategoryRepository;
import fr.doranco.GestionStock.repository.IProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        return ProductResponseDTO.builder()
                .id(savedProduct.getId())
                .name(savedProduct.getName())
                .price(savedProduct.getPrice())
                .actualStock(savedProduct.getActualStock())
                .categoryId(savedProduct.getCategory().getId())
                .reference(savedProduct.getReference())
                .minStock(savedProduct.getMinStock())
                .description(savedProduct.getDescription())
                .imageUrl(savedProduct.getImageUrl())
                .createdAt(savedProduct.getCreatedAt())
                .build();
    }
}
