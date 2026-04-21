package fr.doranco.GestionStock.model.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product must not be null")
    private Product product;

    @Column(nullable = false)
    @NotNull(message = "Quantity must not be null")
    @PositiveOrZero(message = "Quantity must be positive or zero")
    private Integer quantity;

    @Column(nullable = false)
    @NotNull(message = "Unit price must not be null")
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @NotNull(message = "Sale must not be null")
    private Sale sale;
}
