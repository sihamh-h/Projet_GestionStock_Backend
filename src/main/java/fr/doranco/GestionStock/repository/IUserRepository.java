package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.model.entity.Permission;
import fr.doranco.GestionStock.model.entity.Role;
import fr.doranco.GestionStock.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
        Optional<User> findByUsernameIgnoreCase(String username) ;
        Optional<User> findByEmail(String email) ;
        Optional<User> findByPhoneNumber(String phoneNumber) ;

        boolean existsByEmail(String email);
        boolean existsByUsername(String username);
        boolean existsByPhoneNumber(String phoneNumber);


        boolean existsByRoles_Name(UserRole userRole);

        long countByRoles_Name(UserRole roleName);
}

