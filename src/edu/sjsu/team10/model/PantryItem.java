package edu.sjsu.team10.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class PantryItem {

    private final int ingredientId;
    private final String name;
    private final BigDecimal quantity;
    private final String unit;
    private final Timestamp lastUpdate;

    public PantryItem(int ingredientId, String name, BigDecimal quantity,
                      String unit, Timestamp lastUpdate) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.lastUpdate = lastUpdate;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }
}
