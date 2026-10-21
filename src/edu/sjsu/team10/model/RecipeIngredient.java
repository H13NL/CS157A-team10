package edu.sjsu.team10.model;

import java.math.BigDecimal;

public class RecipeIngredient {

    private final int ingredientId;
    private final String name;
    private final BigDecimal quantity;
    private final String unit;
    private final BigDecimal availableQuantity;
    private final String availableUnit;

    public RecipeIngredient(int ingredientId, String name, BigDecimal quantity,
                            String unit, BigDecimal availableQuantity,
                            String availableUnit) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit == null ? "" : unit;
        this.availableQuantity = availableQuantity == null ? BigDecimal.ZERO : availableQuantity;
        this.availableUnit = availableUnit == null ? "" : availableUnit;
    }

    public int getIngredientId() { return ingredientId; }
    public String getName() { return name; }
    public BigDecimal getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public String getAvailableUnit() { return availableUnit; }

    public String getQuantityDisplay() {
        return quantity.stripTrailingZeros().toPlainString();
    }

    public String getAvailableDisplay() {
        return availableQuantity.stripTrailingZeros().toPlainString();
    }

    public boolean isSatisfied() {
        return availableQuantity.compareTo(quantity) >= 0
                && !unit.isEmpty()
                && unit.equalsIgnoreCase(availableUnit);
    }
}
