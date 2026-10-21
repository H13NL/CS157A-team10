package edu.sjsu.team10.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Recipe {

    private final int recipeId;
    private final String name;
    private final String description;
    private final boolean isPublic;
    private final int createdBy;
    private final String creatorName;
    private final Timestamp createdAt;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(int recipeId, String name, String description, boolean isPublic,
                  int createdBy, String creatorName, Timestamp createdAt) {
        this.recipeId = recipeId;
        this.name = name;
        this.description = description == null ? "" : description;
        this.isPublic = isPublic;
        this.createdBy = createdBy;
        this.creatorName = creatorName == null ? "Unknown chef" : creatorName;
        this.createdAt = createdAt;
    }

    public int getRecipeId() { return recipeId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isPublic() { return isPublic; }
    public int getCreatedBy() { return createdBy; }
    public String getCreatorName() { return creatorName; }
    public Timestamp getCreatedAt() { return createdAt; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
    }

    public int getMissingCount() {
        int missing = 0;
        for (RecipeIngredient ingredient : ingredients) {
            if (!ingredient.isSatisfied()) missing++;
        }
        return missing;
    }

    public String getFeasibilityLabel() {
        int missing = getMissingCount();
        if (missing == 0) return "Makeable";
        if (missing <= 2) return "Almost Makeable";
        return "Not Makeable";
    }

    public String getFeasibilityClass() {
        int missing = getMissingCount();
        if (missing == 0) return "makeable";
        if (missing <= 2) return "almost";
        return "not-ready";
    }

    public boolean isOwnedBy(int userId) {
        return !isPublic && createdBy == userId;
    }
}
