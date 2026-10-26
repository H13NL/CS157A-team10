package edu.sjsu.team10.model;

public class Ingredient {

    private int ingId;
    private String name;
    private String unit;

    public Ingredient(int ingId, String name, String unit) {
        this.ingId = ingId;
        this.name = name;
        this.unit = unit;
    }

    public int getIngId() {
        return ingId;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit == null ? "" : unit;
    }
}
