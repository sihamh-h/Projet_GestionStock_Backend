package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.model.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface ICategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    @Query(value = "SELECT * FROM categories WHERE name = :name LIMIT 1", nativeQuery = true)
    Optional<Category> findByNameIncludingDeleted(String name);
}
