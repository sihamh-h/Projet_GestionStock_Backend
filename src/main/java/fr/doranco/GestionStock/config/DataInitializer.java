package fr.doranco.GestionStock.config;


import fr.doranco.GestionStock.enums.UserPermission;
import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.model.entity.Permission;
import fr.doranco.GestionStock.model.entity.Role;
import fr.doranco.GestionStock.repository.IPermissionRepository;
import fr.doranco.GestionStock.repository.IRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private IPermissionRepository permissionRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializePermissions();
        initializeRoles();

    }

    private void initializePermissions() {

            log.info("permission initialization started...");
            for (UserPermission userPermission : UserPermission.values()) {
                if (!permissionRepository.existsByName(userPermission)) {
                    Permission permission = Permission.builder()
                            .name(userPermission)
                            .description(userPermission.getDescription())
                            .build();
                    permissionRepository.save(permission);
                    log.info("Permission {} created", userPermission);
                }
            }

    }

    private void initializeRoles() {

            log.info("role initialization started...");
            for(UserRole userRole: UserRole.values()) {
                if (!roleRepository.existsByName(userRole)) {
                    switch (userRole){
                        case ROLE_ADMIN :
                            Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
                            createRole(userRole, allPermissions);
                            break;
                        case ROLE_MANAGER :
                            Permission permission = permissionRepository
                                    .findByName(UserPermission.USER_READ)
                                    .orElseThrow(() -> new RuntimeException("Permission not found"));
                            Set<Permission> managerPermissions = new HashSet<>();
                            managerPermissions.add(permission);
                            createRole(userRole, managerPermissions);
                            break;
                        case ROLE_EMPLOYEE:
                            Set<Permission> employeePermissions = new HashSet<>();
                            createRole(userRole, employeePermissions);
                            break;
                    }
                }
                }


        }


    private void createRole(UserRole userRole, Set<Permission> permissions) {
            Role role = Role.builder()
                    .name(userRole)
                    .description(userRole.getDescription())
                    .permissions(permissions)
                    .build();
            roleRepository.save(role);
            log.info("Role {} created", userRole);
    }
}
