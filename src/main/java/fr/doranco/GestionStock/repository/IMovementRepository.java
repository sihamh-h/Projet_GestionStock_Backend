package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.model.dto.MovementView;
import fr.doranco.GestionStock.model.entity.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IMovementRepository extends JpaRepository<Movement, Long> {
    List<Movement> findByUserId(Long userId);
    @Query(value = """
    SELECT m.id, m.type, m.quantity, m.product_id,
           p.name as product_name,
           p.deleted as product_deleted,
           m.source_type, m.user_id,
           CONCAT(u.firstname, ' ', u.lastname) as user_full_name,
           m.created_at
    FROM movements m
    LEFT JOIN product p ON p.id = m.product_id
    LEFT JOIN users u ON u.id = m.user_id
    WHERE m.deleted = false
    ORDER BY m.created_at DESC
    """, nativeQuery = true)
    List<MovementView> findAllMovementsWithDetails();


    @Query(value = """
    SELECT m.id, m.type, m.quantity, m.product_id,
           p.name as product_name,
           p.deleted as product_deleted,
           m.source_type, m.user_id,
           CONCAT(u.firstname, ' ', u.lastname) as user_full_name,
           m.created_at
    FROM movements m
    LEFT JOIN product p ON p.id = m.product_id
    LEFT JOIN users u ON u.id = m.user_id
    WHERE m.deleted = false AND m.user_id = :userId
    ORDER BY m.created_at DESC
    """, nativeQuery = true)
    List<MovementView> findMovementsByUserIdWithDetails(@Param("userId") Long userId);
}
