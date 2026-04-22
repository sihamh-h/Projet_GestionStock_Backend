package fr.doranco.GestionStock.repository;


import fr.doranco.GestionStock.model.entity.Product;
import fr.doranco.GestionStock.model.entity.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IStockAlertRepository extends JpaRepository<StockAlert, Long> {
    // Toutes les alertes non résolues (pour le dashboard)
    List<StockAlert> findByResolvedAtIsNullOrderByCreatedAtDesc();

    // Vérifie si une alerte active existe déjà pour ce produit (évite les doublons)
    boolean existsByProductAndResolvedAtIsNull(Product product);

    // Pour résoudre les alertes d'un produit quand le stock remonte
    Optional<StockAlert> findByProductAndResolvedAtIsNull(Product product);
}
