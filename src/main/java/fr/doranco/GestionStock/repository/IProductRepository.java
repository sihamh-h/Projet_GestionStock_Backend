package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {

    Set<Product> findByCategoryId(Long categoryId);

    boolean existsByReference(String reference);

    @Query("SELECT p FROM Product p WHERE p.actualStock <= p.minStock")
    Set<Product> findLowStockProducts();

    @Query(value = "SELECT * FROM product WHERE reference = :reference LIMIT 1", nativeQuery = true)
    Optional<Product> findByReferenceIncludingDeleted(String reference);

    @Query(value = "SELECT * FROM product WHERE id = :id", nativeQuery = true)
    Optional<Product> findByIdIncludingDeleted(@Param("id") Long id);

    @Query(value = "SELECT * FROM product WHERE deleted = true", nativeQuery = true)
    List<Product> findAllDeleted();
}
