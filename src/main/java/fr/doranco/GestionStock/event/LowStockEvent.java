package fr.doranco.GestionStock.event;

import fr.doranco.GestionStock.model.entity.Product;
import lombok.Getter;


@Getter
public class LowStockEvent {

    private final Product product;

    public LowStockEvent(Product product) {
        this.product = product;
    }
}
