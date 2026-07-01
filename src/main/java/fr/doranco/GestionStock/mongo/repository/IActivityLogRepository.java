package fr.doranco.GestionStock.mongo.repository;

import fr.doranco.GestionStock.mongo.document.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IActivityLogRepository extends MongoRepository<ActivityLog, String> {
    List<ActivityLog> findByAction(String action);

    List<ActivityLog> findByUserId(Long userId);

    List<ActivityLog> findBySuccessFalse();
}
