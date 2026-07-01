package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.ProductRequestDTO;
import fr.doranco.GestionStock.model.dto.ProductResponseDTO;
import fr.doranco.GestionStock.model.dto.ProductUpdateDTO;
import fr.doranco.GestionStock.model.entity.Category;
import fr.doranco.GestionStock.model.entity.Product;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.mongo.dto.ActivityLogRequestDTO;
import fr.doranco.GestionStock.mongo.service.IActivityLogService;
import fr.doranco.GestionStock.repository.ICategoryRepository;
import fr.doranco.GestionStock.repository.IProductRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import fr.doranco.GestionStock.utils.RequestUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductService implements IProductService {
    private final ICategoryRepository categoryRepository;
    private final IProductRepository productRepository;
    private final IActivityLogService activityLogService;
    private final IUserRepository userRepository;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO request) throws Exception {
        if (request == null) {
            log.error("[PRODUCT] Creation attempt with a null request");
            throw new IllegalArgumentException("Request is null");
        }
        if (request.reference() == null || request.name() == null || request.name().isBlank()
                || request.price() == null || request.price().compareTo(BigDecimal.ZERO) < 0
                || request.actualStock() == null || request.actualStock() < 0
                || request.categoryId() == null || request.categoryId() < 0
                || request.minStock() == null || request.minStock() < 0) {
            log.error("[PRODUCT] Creation attempt with invalid data: name={}, price={}, actualStock={}, categoryId={}, minStock={}",
                    request.name(), request.price(), request.actualStock(), request.categoryId(), request.minStock());
            throw new IllegalArgumentException("Invalid product data: name, price, actualStock," +
                    " categoryId and minStock must be provided and valid");
        }
        try {
            Optional<Product> existingProduct = productRepository.findByReferenceIncludingDeleted(request.reference());

            if (existingProduct.isPresent()) {
                Product product = existingProduct.get();
                if (!product.isDeleted()) {
                    log.error("[PRODUCT] Creation attempt with duplicate reference: {}", request.reference());
                    activityLogService.log(ActivityLogRequestDTO.builder()
                            .userId(RequestUtils.getCurrentUserId())
                            .userEmail(RequestUtils.getCurrentUserEmail())
                            .action("PRODUCT_CREATE")
                            .description("Échec création produit : référence déjà existante " + request.reference())
                            .success(false)
                            .build());
                    throw new IllegalArgumentException("Product with the same reference already exists");
                }

                // Cas 2 : produit soft-deleted → on le restaure avec les nouvelles données
                log.warn("[PRODUCT] Creation attempt with reference of a deleted product. Restoring product with ID {}: {}", product.getId(), product);
                product.setDeleted(false);
                product.setName(request.name());
                product.setPrice(request.price());
                product.setActualStock(request.actualStock());
                product.setMinStock(request.minStock());
                product.setDescription(request.description());
                product.setImageUrl(request.imageUrl());

                Category category = findCategory(request.categoryId());
                product.setCategory(category);

                Product savedProduct = productRepository.save(product);

                activityLogService.log(ActivityLogRequestDTO.builder()
                        .userId(savedProduct.getCreatedBy())
                        .userEmail(RequestUtils.getCurrentUserEmail())
                        .action("PRODUCT_CREATE")
                        .description("Restored deleted product with reference " + savedProduct.getReference())
                        .success(true)
                        .build());

                log.info("[PRODUCT] Success: Deleted product restored with ID {}", savedProduct.getId());
                return mapToResponseDto(savedProduct);
            }

            // Cas 3 : nouvelle référence → création normale
            Category category = findCategory(request.categoryId());
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

            activityLogService.log(ActivityLogRequestDTO.builder()
                    .userId(savedProduct.getCreatedBy())
                    .userEmail(RequestUtils.getCurrentUserEmail())
                    .action("PRODUCT_CREATE")
                    .description("Created product with reference " + savedProduct.getReference())
                    .success(true)
                    .build());

            log.info("[PRODUCT] Success: Product created with ID {}", savedProduct.getId());
            return mapToResponseDto(savedProduct);

        } catch (IllegalArgumentException e) {
            // Re-lancée telle quelle → GlobalExceptionHandler → 409
            log.error("[PRODUCT] Validation error during product creation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[PRODUCT] Unexpected error during product creation: {}", e.getMessage());
            activityLogService.log(ActivityLogRequestDTO.builder()
                    .userId(RequestUtils.getCurrentUserId())
                    .userEmail(RequestUtils.getCurrentUserEmail())
                    .action("PRODUCT_CREATE")
                    .description("Échec création produit : " + request.name() + " - " + e.getMessage())
                    .success(false)
                    .build());
            throw new Exception("Failed to create product", e);
        }
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
        try {
            if (id == null || id < 0) {
                log.error("[PRODUCT] Update attempt with invalid ID: {}", id);
                throw new IllegalArgumentException("Invalid product ID: " + id);
            }
            if (dto == null) {
                log.error("[PRODUCT] Update attempt with a null request");
                throw new IllegalArgumentException("Request is null");
            }
            Product existingProduct = productRepository.findById(id).orElseThrow(() -> {
                log.warn("[PRODUCT] Update failed: No product found with ID {}", id);
                return new IllegalArgumentException("Product with ID " + id + " not found");
            });
            if (dto.reference() != null && !dto.reference().equals(existingProduct.getReference())) {
                if (productRepository.existsByReference(dto.reference())) {
                    log.warn("[PRODUCT] Update failed: A product with reference '{}' already exists in the database.", dto.reference());
                    throw new IllegalArgumentException("Product with reference " + dto.reference() + " already exists");
                }
                existingProduct.setReference(dto.reference());
            }
            if (dto.categoryId() != null && !dto.categoryId().equals(existingProduct.getCategory().getId())) {
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
            if (dto.imageUrl() != null) {
                existingProduct.setImageUrl(dto.imageUrl());
            }

            Product updatedProduct = productRepository.save(existingProduct);
            log.info("[PRODUCT] Success: Product with ID {} updated", id);

            activityLogService.log(ActivityLogRequestDTO.builder()
                    .userId(updatedProduct.getUpdatedBy())
                    .userEmail(RequestUtils.getCurrentUserEmail())
                    .action("PRODUCT_UPDATE")
                    .description("updated product " + updatedProduct.getName())
                    .success(true)
                    .build());
            return mapToResponseDto(updatedProduct);
        } catch (Exception e) {
            log.error("[PRODUCT] Failed to update product with ID {}: {}", id, e.getMessage());
            activityLogService.log(ActivityLogRequestDTO.builder()
                    .userId(RequestUtils.getCurrentUserId())
                    .userEmail(RequestUtils.getCurrentUserEmail())
                    .action("PRODUCT_UPDATE")
                    .description("Échec mise à jour produit " + dto.name() + " : " + e.getMessage())
                    .success(false)
                    .build());
            throw new Exception("Failed to update product with ID " + id, e);
        }

    }

    @Override
    public void deleteProduct(Long id) throws Exception {
        try {
            if (id == null || id < 0) {
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

            activityLogService.log(ActivityLogRequestDTO.builder()
                    .userId(product.getUpdatedBy())
                    .userEmail(RequestUtils.getCurrentUserEmail())
                    .action("PRODUCT_DELETE")
                    .description("deleted product: " + product.getName())
                    .success(true)
                    .build());
        }catch (Exception e){
            activityLogService.log(ActivityLogRequestDTO.builder()
                    .userId(RequestUtils.getCurrentUserId())
                    .userEmail(RequestUtils.getCurrentUserEmail())
                    .action("PRODUCT_DELETE")
                    .description("Echec deletion produit ID " + id + " : " + e.getMessage())
                    .success(false)
                    .build());
            throw e;
        }
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
    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("[PRODUCT] Category not found with ID {}", categoryId);
                    return new EntityNotFoundException("Category with ID " + categoryId + " not found");
                });
    }
    @Override
    public ProductResponseDTO restoreProduct(Long id) throws Exception {
        Product product = productRepository
                .findByIdIncludingDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));

        if (!product.isDeleted()) {
            throw new IllegalStateException("Product is not deleted");
        }

        product.setDeleted(false);
        product.setDeletedAt(null);
        return mapToResponseDto(productRepository.save(product));
    }

    @Override
    public List<ProductResponseDTO> getDeletedProducts() throws Exception {
        return productRepository.findAllDeleted()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }
}
