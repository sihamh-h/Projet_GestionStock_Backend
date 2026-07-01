package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.model.entity.PasswordHistory;
import fr.doranco.GestionStock.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop3ByUserOrderByCreatedAtDesc(User user);
}
