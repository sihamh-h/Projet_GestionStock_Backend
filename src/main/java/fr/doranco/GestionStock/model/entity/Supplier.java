package fr.doranco.GestionStock.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Supplier extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    @NotBlank(message = "Supplier number cannot be blank")
    @Size (max = 50, message = "Supplier number must be at most 50 characters")
    private String number;

    @Column(length = 50, nullable = false)
    @NotBlank(message = "Supplier name cannot be blank")
    @Size(max = 50, message = "Supplier name must be at most 50")
    private String name;

    @Column(name= "company_name", length = 50)
    @Size(max = 50, message = "Company name must be at most 50 characters")
    private String companyName;

    @Column(length = 50)
    @Size(max = 50, message = "website must be at most 50 characters")
    private String website;

    @Column(length = 14, unique = true)
    @Pattern(regexp= "^\\d{14}$", message = "SIRET must be exactly 14 digits")
    private String siret;

    @Column(length = 100)
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @Column(name = "phone_number", length = 20, nullable = false)
    @NotBlank(message = "phone number is obligatory")
    @Pattern( regexp = "^(?:(?:\\+|00)33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}$", message = "Phone number must be a valid French number")
    private String phoneNumber;

    @Column(length = 100)
    @Size(max = 100, message = "Address must be at most 100 characters")
    private String address;

    @OneToMany(mappedBy = "supplier", fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

}
