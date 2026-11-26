package edu.sjsu.team10.model;

import java.math.BigDecimal;

import edu.sjsu.team10.util.Forms;

// One ingredient in a restocker's catalog (a row of the supplies table).
public class CatalogItem {

    private final int ingId;
    private final String name;
    private final String unit;
    private final BigDecimal price; // per unit
    private final BigDecimal qty;   // amount the restocker has available

    public CatalogItem(int ingId, String name, String unit, BigDecimal price, BigDecimal qty) {
        this.ingId = ingId;
        this.name = name;
        this.unit = unit == null ? "" : unit;
        this.price = price;
        this.qty = qty;
    }

    public int getIngId() { return ingId; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getQty() { return qty; }

    public String getPriceDisplay() { return price.setScale(2).toPlainString(); }
    public String getQtyDisplay() { return Forms.plain(qty); }
    public boolean isAvailable() { return qty.signum() > 0; }
}
