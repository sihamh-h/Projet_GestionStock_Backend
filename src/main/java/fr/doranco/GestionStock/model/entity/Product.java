package fr.doranco.GestionStock.model.entity;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;



import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder
@SQLRestriction("deleted = false")
public class Product extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String reference;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String description;

    @Column(nullable = false)
    @PositiveOrZero(message = "Price must be positive or zero")
    @NotNull(message = "Price must not be null")
    private Double price;

    @Column(name ="actual_stock", nullable = false)
    @NotNull(message = "Actual stock must not be null")
    @PositiveOrZero(message = "Actual stock must be positive or zero")
    private Integer actualStock;

    @Column(name = "min_stock", nullable = false)
    @NotNull(message = "Minimum stock must not be null")
    @PositiveOrZero(message = "Minimum stock must be positive or zero")
    private Integer minStock;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Movement> movements = new ArrayList<>();


}
