package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {

    Set<Product> findByCategoryId(Long categoryId);

    boolean existsByReference(String reference);

    @Query("SELECT p FROM Product p WHERE p.actualStock <= p.minStock")
    Set<Product> findLowStockProducts();
}
