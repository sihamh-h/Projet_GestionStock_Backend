package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.model.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ISupplierRepository extends JpaRepository<Supplier, Long> {
     List<Supplier> findAllByDeletedFalse();
     Optional<Supplier> findByIdAndDeletedFalse(Long id);
     boolean existsByNumberAndDeletedFalse(String number);
     boolean existsBySiretAndDeletedFalse(String siret);
}

