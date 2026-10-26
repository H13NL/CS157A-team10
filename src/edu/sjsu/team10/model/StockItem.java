package edu.sjsu.team10.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

// One row of a restaurant's inventory (stock joined with ingredient)
public class StockItem {

    private int ingId;
    private String name;
    private BigDecimal qtyAvai;
    private String unit;
    private Timestamp lastUpdate;

    public StockItem(int ingId, String name, BigDecimal qtyAvai, String unit, Timestamp lastUpdate) {
        this.ingId = ingId;
        this.name = name;
        this.qtyAvai = qtyAvai;
        this.unit = unit;
        this.lastUpdate = lastUpdate;
    }

    public int getIngId() {
        return ingId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getQtyAvai() {
        return qtyAvai;
    }

    public String getUnit() {
        return unit == null ? "" : unit;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public boolean isInStock() {
        return qtyAvai.signum() > 0;
    }

    // Quantity without trailing zeros, e.g. 5.00 -> "5", 2.50 -> "2.5"
    public String getQtyDisplay() {
        return qtyAvai.stripTrailingZeros().toPlainString();
    }
}
