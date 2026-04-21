package fr.doranco.GestionStock.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Sale extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Sale number must not be blank")
    @Column(name = "sale_number", unique = true, nullable = false)
    private String saleNumber;

    @Column(nullable = false)
    private LocalDate saleDate;

    @Column(name = "total_amount", nullable = false)
    @NotNull(message = "total amount must not be null")
    @PositiveOrZero(message = "total amount must be positive or zero")
    private Float totalAmount;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
    private List<SaleLine> lines = new ArrayList<>();
}
