package fr.doranco.GestionStock.mongo.repository;

import fr.doranco.GestionStock.mongo.document.ActivityLog;
import fr.doranco.GestionStock.mongo.document.InvalidatedToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IInvalidatedTokenRepository extends MongoRepository<InvalidatedToken, String> {
        boolean existsByToken(String token);

}
