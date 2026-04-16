package fr.doranco.GestionStock.enums;

import lombok.Getter;

@Getter
public enum UserPermission {
    USER_READ("View users"),
    USER_CREATE("Create a user"),
    USER_UPDATE("Update a user"),
    USER_DELETE("Delete a user"),

    PRODUCT_READ("View products"),
    PRODUCT_CREATE("Create a product"),
    PRODUCT_UPDATE("Update a product"),
    PRODUCT_DELETE("Delete a product"),

    STOCK_READ("View stock"),
    STOCK_CREATE("Add new products to stock"),
    STOCK_UPDATE("Update stock information"),
    STOCK_DELETE("Remove products from stock"),

    MOVEMENT_RECORD("Record stock movements"),
    INVENTORY_ADJUST("Adjust inventory quantities");

    private final String description;

    UserPermission(String description) {
        this.description = description;
    }

}

/*
// Gestion des Utilisateurs (Admin uniquement)
    USER_READ("Lire les informations des utilisateurs"),
    USER_CREATE("Créer de nouveaux utilisateurs"),
    USER_UPDATE("Modifier les utilisateurs"),
    USER_DELETE("Supprimer des utilisateurs"),

    // Gestion du Stock / Produits
    STOCK_READ("Consulter l'état du stock"),
    STOCK_CREATE("Ajouter de nouveaux produits"),
    STOCK_UPDATE("Modifier les fiches produits"),
    STOCK_DELETE("Retirer des produits du catalogue"),

    // Mouvements de stock (Entrées/Sorties)
    INVENTORY_ADJUST("Ajuster les quantités (inventaire)"),
    MOVEMENT_RECORD("Enregistrer des entrées/sorties"),


    // Product management
    PRODUCT_READ("View products"),
    PRODUCT_CREATE("Create a product"),
    PRODUCT_UPDATE("Update a product"),
    PRODUCT_DELETE("Delete a product"),

    // Stock management
    STOCK_READ("View stock"),
    STOCK_UPDATE("Update stock"),
    STOCK_EXPORT("Export stock report"),

    // Order management
    ORDER_READ("View orders"),
    ORDER_CREATE("Create an order"),
    ORDER_UPDATE("Update an order"),
    ORDER_DELETE("Delete an order"),

    // Supplier management
    SUPPLIER_READ("View suppliers"),
    SUPPLIER_CREATE("Create a supplier"),
    SUPPLIER_UPDATE("Update a supplier"),
    SUPPLIER_DELETE("Delete a supplier"),

    // User management (admin only)
    USER_READ("View users"),
    USER_CREATE("Create a user"),
    USER_UPDATE("Update a user"),
    USER_DELETE("Delete a user");
 // ========================
    // PRODUITS
    // ========================
    CREATE_PRODUCT("Créer un produit"),
    UPDATE_PRODUCT("Modifier un produit"),
    DELETE_PRODUCT("Supprimer un produit"),
    VIEW_PRODUCT("Consulter les produits"),

    // ========================
    // STOCK
    // ========================
    ADD_STOCK("Ajouter du stock"),
    REMOVE_STOCK("Retirer du stock"),
    ADJUST_STOCK("Ajuster le stock"),
    VIEW_STOCK("Consulter le stock"),

    // ========================
    // FOURNISSEURS
    // ========================
    CREATE_SUPPLIER("Créer un fournisseur"),
    UPDATE_SUPPLIER("Modifier un fournisseur"),
    DELETE_SUPPLIER("Supprimer un fournisseur"),
    VIEW_SUPPLIER("Consulter les fournisseurs"),

    // ========================
    // COMMANDES
    // ========================
    CREATE_ORDER("Créer une commande"),
    UPDATE_ORDER("Modifier une commande"),
    CANCEL_ORDER("Annuler une commande"),
    VALIDATE_ORDER("Valider une commande"),
    VIEW_ORDER("Consulter les commandes"),

    // ========================
    // UTILISATEURS
    // ========================
    CREATE_USER("Créer un utilisateur"),
    UPDATE_USER("Modifier un utilisateur"),
    DELETE_USER("Supprimer un utilisateur"),
    VIEW_USER("Consulter les utilisateurs"),

    // ========================
    // ROLES & PERMISSIONS
    // ========================
    ASSIGN_ROLE("Assigner un rôle"),
    MANAGE_ROLES("Gérer les rôles"),
    MANAGE_PERMISSIONS("Gérer les permissions"),

    // ========================
    // REPORTING
    // ========================
    VIEW_REPORTS("Consulter les rapports"),
    EXPORT_REPORTS("Exporter les rapports"),

    // ========================
    // SYSTEME
    // ========================
    ACCESS_ADMIN_PANEL("Accéder au panneau d'administration"),
    FULL_ACCESS("Accès complet au système");

 */
