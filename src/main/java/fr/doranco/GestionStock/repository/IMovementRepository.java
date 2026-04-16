package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.model.entity.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMovementRepository extends JpaRepository<Movement, Long> {
}
