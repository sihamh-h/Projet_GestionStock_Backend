package fr.doranco.GestionStock.mongo.dto;


import lombok.Builder;

@Builder
public record ActivityLogRequestDTO(Long userId,
                                    String userEmail,
                                    String action,
                                    String description,
                                    boolean success) {


}
