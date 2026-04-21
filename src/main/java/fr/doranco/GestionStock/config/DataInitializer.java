package fr.doranco.GestionStock.config;


import fr.doranco.GestionStock.enums.UserPermission;
import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.model.entity.Permission;
import fr.doranco.GestionStock.model.entity.Role;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.repository.IPermissionRepository;
import fr.doranco.GestionStock.repository.IRoleRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final IPermissionRepository permissionRepository;

    private final IRoleRepository roleRepository;

    private final IUserRepository userRepository;

  /*  @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Data initialization started...");
        initializePermissions();
        initializeRoles();
        log.info("Data initialization completed.");

    }*/

    @Override
    @Transactional
    // On met le @Transactional ici pour que toute l'initialisation soit une seule unité de travail
    public void run(String... args) {
        log.info("--- Démarrage de la synchronisation des données de sécurité ---");

        Map<UserPermission, Permission> syncPermissions = syncPermissions();
        syncRoles(syncPermissions);
        cleanupObsoletePermissions();
        cleanupObsoleteRoles();

        log.info("--- Synchronisation terminée avec succès ---");
    }

    /*private void initializePermissions() {

            log.info("permission initialization started...");
            for (UserPermission userPermission : UserPermission.values()) {
                //if (!permissionRepository.existsByName(userPermission)) {
                    Permission permission = Permission.builder()
                            .name(userPermission)
                            .description(userPermission.getDescription())
                            .build();
                    permissionRepository.save(permission);
                    log.info("Permission {} created", userPermission);
                //}
            }

    }*/

    /**
     * Synchronise l'Enum UserPermission avec la table Permission en BDD.
     * Si une permission de l'Enum n'est pas en BDD, elle est créée.
     * Si elle existe, on met à jour sa description.
     */
    private Map<UserPermission, Permission> syncPermissions() {
        List<Permission> existingPermissions = permissionRepository.findAll();
        Map<UserPermission, Permission> permissionMap = existingPermissions.stream()
                .collect(Collectors.toMap(Permission::getName, p -> p));

        List<Permission> toSave = new ArrayList<>();

        for (UserPermission enumPerm : UserPermission.values()) {
            Permission p = permissionMap.getOrDefault(enumPerm, new Permission());
            p.setName(enumPerm);
            p.setDescription(enumPerm.getDescription());
            toSave.add(p);
        }

        List<Permission> saved = permissionRepository.saveAll(toSave);
        log.info("{} permissions synchronisées.", saved.size());

        // On retourne une map à jour pour la phase suivante (les rôles)
        return saved.stream().collect(Collectors.toMap(Permission::getName, p -> p));
    }

    /*private void initializeRoles() {

            log.info("role initialization started...");
            for(UserRole userRole: UserRole.values()) {
                //if (!roleRepository.existsByName(userRole)) {
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
                //}
                }


        }*/

    /**
     * Synchronise les rôles et leurs attributions de permissions.
     */
    private void syncRoles(Map<UserPermission, Permission> allPermissionsMap) {
        for (UserRole enumRole : UserRole.values()) {
            Role role = roleRepository.findByName(enumRole)
                    .orElseGet(() -> Role.builder().name(enumRole).build());

            role.setDescription(enumRole.getDescription());

            // On définit les permissions cibles pour ce rôle
            Set<Permission> targetPermissions = determinePermissions(enumRole, allPermissionsMap);

            // Mise à jour de la relation ManyToMany
            role.setPermissions(targetPermissions);
            roleRepository.save(role);

            log.info("Rôle [{}] synchronisé avec {} permissions.", enumRole, targetPermissions.size());
        }
    }


    /*rivate void createRole(UserRole userRole, Set<Permission> permissions) {
            Role role = Role.builder()
                    .name(userRole)
                    .description(userRole.getDescription())
                    .permissions(permissions)
                    .build();
            roleRepository.save(role);
            log.info("Role {} created", userRole);
    }*/
    /**
     * Logique métier de distribution des permissions par rôle.
     */
    private Set<Permission> determinePermissions(UserRole role, Map<UserPermission, Permission> allPermissions) {
        Set<Permission> perms = new HashSet<>();

        switch (role) {
            case ROLE_ADMIN:
                // L'admin reçoit absolument TOUT
                perms.addAll(allPermissions.values());
                break;

            case ROLE_MANAGER:
                // Le Manager peut lire les users et gérer les produits
                addPerm(perms, allPermissions, UserPermission.USER_READ);
                addPerm(perms, allPermissions, UserPermission.PRODUCT_READ);
                addPerm(perms, allPermissions, UserPermission.PRODUCT_CREATE);
                addPerm(perms, allPermissions, UserPermission.PRODUCT_UPDATE);
                addPerm(perms, allPermissions, UserPermission.PRODUCT_DELETE);
                addPerm(perms, allPermissions, UserPermission.CATEGORY_CREATE);
                addPerm(perms, allPermissions, UserPermission.CATEGORY_UPDATE);
                addPerm(perms, allPermissions, UserPermission.CATEGORY_DELETE);
                addPerm(perms, allPermissions, UserPermission.CATEGORY_READ);
                addPerm(perms, allPermissions, UserPermission.STOCK_READ);
                addPerm(perms, allPermissions, UserPermission.STOCK_CREATE);
                addPerm(perms, allPermissions, UserPermission.STOCK_UPDATE);
                addPerm(perms, allPermissions, UserPermission.STOCK_DELETE);
                addPerm(perms, allPermissions, UserPermission.STOCK_READ);
                addPerm(perms, allPermissions, UserPermission.SUPPLIER_READ);
                addPerm(perms, allPermissions, UserPermission.SUPPLIER_CREATE);
                addPerm(perms, allPermissions, UserPermission.SUPPLIER_UPDATE);
                addPerm(perms, allPermissions, UserPermission.SUPPLIER_DELETE);
                addPerm(perms, allPermissions, UserPermission.ORDER_READ);
                addPerm(perms, allPermissions, UserPermission.ORDER_CREATE);
                addPerm(perms, allPermissions, UserPermission.ORDER_UPDATE);
                addPerm(perms, allPermissions, UserPermission.ORDER_DELETE);
                addPerm(perms, allPermissions, UserPermission.MOVEMENT_RECORD);
                addPerm(perms, allPermissions, UserPermission.INVENTORY_ADJUST);
                addPerm(perms, allPermissions, UserPermission.MOVEMENT_READ);
                addPerm(perms, allPermissions, UserPermission.SALE_CREATE);

                break;

            case ROLE_EMPLOYEE:
                addPerm(perms, allPermissions, UserPermission.PRODUCT_READ);
                addPerm(perms, allPermissions, UserPermission.PRODUCT_CREATE);
                addPerm(perms, allPermissions, UserPermission.PRODUCT_UPDATE);
                addPerm(perms, allPermissions, UserPermission.PRODUCT_DELETE);
                addPerm(perms, allPermissions, UserPermission.STOCK_READ);
                addPerm(perms, allPermissions, UserPermission.MOVEMENT_RECORD);
                addPerm(perms, allPermissions, UserPermission.INVENTORY_ADJUST);
                addPerm(perms, allPermissions, UserPermission.MOVEMENT_READ);
                addPerm(perms, allPermissions, UserPermission.SALE_CREATE);
                break;
        }
        return perms;
    }

    // Petite méthode utilitaire pour éviter les NullPointer
    private void addPerm(Set<Permission> set, Map<UserPermission, Permission> map, UserPermission up) {
        if (map.containsKey(up)) {
            set.add(map.get(up));
        }
    }

    // -------------------------------------------------------
    // 4) Suppression des permissions obsolètes
    // -------------------------------------------------------
    private void cleanupObsoletePermissions() {
        Set<UserPermission> enumPermissions = Set.of(UserPermission.values());

        List<Permission> obsolete = permissionRepository.findAll().stream()
                .filter(p -> !enumPermissions.contains(p.getName()))
                .toList();

        if (!obsolete.isEmpty()) {
            // 1. On retire ces permissions de tous les rôles d'abord
            List<Role> allRoles = roleRepository.findAll();
            allRoles.forEach(role -> role.getPermissions().removeAll(obsolete));
            roleRepository.saveAll(allRoles);

            // 2. Maintenant on peut supprimer physiquement
            permissionRepository.deleteAll(obsolete);
        }
    }

    // -------------------------------------------------------
    // 5) Suppression des rôles obsolètes
    // -------------------------------------------------------
    private void cleanupObsoleteRoles() {
        Set<UserRole> enumRoles = Set.of(UserRole.values());

        List<Role> obsolete = roleRepository.findAll().stream()
                .filter(r -> !enumRoles.contains(r.getName()))
                .toList();

        if (!obsolete.isEmpty()) {
            log.warn("Nettoyage des rôles obsolètes : {}", obsolete);

            // 1. On délie les rôles des utilisateurs (si ta relation est ManyToMany)
            // Ou on met le rôle à null/défaut chez les users concernés
            List<User> usersWithObsoleteRoles = userRepository.findAllByRolesIn(obsolete);
            usersWithObsoleteRoles.forEach(u -> u.getRoles().removeAll(obsolete));
            userRepository.saveAll(usersWithObsoleteRoles);

            // 2. Maintenant on peut supprimer les rôles
            roleRepository.deleteAll(obsolete);
        }
    }
}
