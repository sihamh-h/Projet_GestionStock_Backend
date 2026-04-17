package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.model.entity.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IMovementRepository extends JpaRepository<Movement, Long> {
    List<Movement> findByUserId(Long userId);
}
