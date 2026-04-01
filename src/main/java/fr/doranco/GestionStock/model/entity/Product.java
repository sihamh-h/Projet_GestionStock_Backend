package fr.doranco.GestionStock.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;


import java.time.LocalDate;

@Entity
//@Table(name = "product")
//@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*@Column(nullable = false, length = 50, unique = true)
    private String reference;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String description;

    @Column(nullable = false)
    @PositiveOrZero(message = "Price must be positive or zero")
    @NotNull(message = "Price must not be null")
    private Float price;
    private int actualStock;
    private int minStock;
    private String imageUrl;
    private LocalDate creationDate;
    private Long createdBy;*/


}
