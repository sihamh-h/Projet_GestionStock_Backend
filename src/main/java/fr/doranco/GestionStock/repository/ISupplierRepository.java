package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.model.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ISupplierRepository extends JpaRepository<Supplier, Long> {
     List<Supplier> findAllByDeletedFalse();
     Optional<Supplier> findByIdAndDeletedFalse(Long id);
     boolean existsByNumberAndDeletedFalse(String number);
     boolean existsBySiretAndDeletedFalse(String siret);
     @Query(value = "SELECT * FROM suppliers WHERE number = :number OR siret = :siret LIMIT 1", nativeQuery = true)
     Optional<Supplier> findByNumberOrSiretIncludingDeleted(String number, String siret);
}

