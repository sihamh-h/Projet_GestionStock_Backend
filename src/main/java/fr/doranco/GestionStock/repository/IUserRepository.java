package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.enums.UserRole;

import fr.doranco.GestionStock.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;


import java.util.Optional;


@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email) ;
        boolean existsByEmail(String email);
        boolean existsByUsername(String username);
        boolean existsByPhoneNumber(String phoneNumber);


        boolean existsByRoles_Name(UserRole userRole);

        long countByRoles_Name(UserRole roleName);

        boolean existsByEmailAndIdNot(String email, Long id);
        boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
}

