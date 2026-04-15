package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.model.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ICategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}
