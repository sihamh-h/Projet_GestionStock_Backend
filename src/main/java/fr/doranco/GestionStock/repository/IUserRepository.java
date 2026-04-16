package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.enums.UserRole;

import fr.doranco.GestionStock.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);

        boolean existsByEmail(String email);

        boolean existsByUsername(String username);

        boolean existsByPhoneNumber(String phoneNumber);


        boolean existsByRoles_Name(UserRole userRole);

        long countByRoles_Name(UserRole roleName);

        boolean existsByEmailAndIdNot(String email, Long id);

        boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);

        @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
        Optional<User> findByEmailWithRoles(@Param("email") String email);

        /*@Query(value = "SELECT * FROM users WHERE deleted = true AND updated_at <= :limitDate AND anonymized_at IS NULL", nativeQuery = true)*/
        //List<User> findUsersReadyForAnonymization(@Param("limitDate") LocalDateTime limitDate);

        @Query(value = " select  * from users where deleted = true and updated_at <= :limitDate and anonymized_at is null", nativeQuery = true)
        List<User> findUsersReadyForAnonymization(@Param("limitDate") LocalDateTime limitDate);

        @Query(value = "SELECT COUNT(*) FROM users WHERE email = :email", nativeQuery = true)
        Long countByEmailIncludingDeleted(@Param("email") String email);

        @Query(value = "SELECT COUNT(*) FROM users WHERE username = :username", nativeQuery = true)
        Long countByUsernameIncludingDeleted(@Param("username") String username);

        @Query(value = "SELECT COUNT(*) FROM users WHERE phone_number = :phone", nativeQuery = true)
        Long countByPhoneNumberIncludingDeleted(@Param("phone") String phone);

}

