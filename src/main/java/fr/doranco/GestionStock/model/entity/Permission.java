package fr.doranco.GestionStock.model.entity;

import fr.doranco.GestionStock.enums.UserPermission;
import jakarta.persistence.*;
import lombok.*;


import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Setter @Getter @Builder @AllArgsConstructor @NoArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    @Enumerated(EnumType.STRING)
    private UserPermission name;

    @Column(length = 100)
    private String description;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
}
