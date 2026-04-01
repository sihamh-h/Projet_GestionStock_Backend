package fr.doranco.GestionStock.repository;

import fr.doranco.GestionStock.enums.UserPermission;
import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPermissionRepository extends JpaRepository<Permission, Long> {
     Optional<Permission> findByName(UserPermission name) ;
     boolean existsByName(UserPermission name);
}
