package edu.sjsu.team10.model;

import java.math.BigDecimal;

import edu.sjsu.team10.util.Forms;

// One shared ingredient that a consumer can place in a grocery order.
// pantryQuantity is null when the consumer does not currently keep the ingredient.
public class OrderIngredient {

    private final int ingredientId;
    private final String name;
    private final String unit;
    private final boolean inPantry;
    private final BigDecimal pantryQuantity;

    public OrderIngredient(int ingredientId, String name, String unit,
                           boolean inPantry, BigDecimal pantryQuantity) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.unit = unit == null ? "" : unit;
        this.inPantry = inPantry;
        this.pantryQuantity = pantryQuantity;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isInPantry() {
        return inPantry;
    }

    public BigDecimal getPantryQuantity() {
        return pantryQuantity;
    }

    public String getPantryQuantityDisplay() {
        return Forms.plain(pantryQuantity);
    }
}
