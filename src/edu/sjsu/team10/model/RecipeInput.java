package edu.sjsu.team10.model;

import java.math.BigDecimal;

public class RecipeInput {
    private final Integer ingredientId;
    private final String ingredientName;
    private final BigDecimal quantity;
    private final String unit;

    public RecipeInput(Integer ingredientId, String ingredientName,
                       BigDecimal quantity, String unit) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Integer getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public BigDecimal getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}
