package fr.doranco.GestionStock.model.dto;

public interface MovementView {
    Long getId();
    String getType();
    Integer getQuantity();
    Long getProductId();
    String getProductName();
    Boolean getProductDeleted();
    String getSourceType();
    Long getUserId();
    String getUserFullName();
    String getCreatedAt();
}
