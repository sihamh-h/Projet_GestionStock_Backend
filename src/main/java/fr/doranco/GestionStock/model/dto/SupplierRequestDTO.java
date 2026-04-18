package fr.doranco.GestionStock.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record SupplierRequestDTO(@NotBlank(message = "Supplier number is required")
                                  @Size(max = 50)
                                  String number,

                                 @NotBlank(message = "Supplier name is required")
                                  @Size(min = 2, max = 50)
                                  String name,

                                 @Size(max = 50)
                                  String companyName,

                                 @Size(max = 50)
                                  String website,

                                 @Pattern(regexp = "^\\d{14}$", message = "SIRET must be exactly 14 digits")
                                  String siret,

                                  @Email(message = "Invalid email format")
                                  @Size(max = 100)
                                  String email,

                                  @Pattern( regexp = "^(?:(?:\\+|00)33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}$", message = "Phone number must be a valid French number")
                                  @NotBlank(message = "Phone number is required")
                                  @Size(max = 20)
                                  String phone,

                                 @Size(max = 100)
                                  String address) {
}
