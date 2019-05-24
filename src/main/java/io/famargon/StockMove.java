package io.famargon;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Order
 */
@RegisterForReflection
public class StockMove {

    String id;
    String item;
    int quantity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}