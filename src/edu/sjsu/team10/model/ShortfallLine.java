package edu.sjsu.team10.model;

import java.math.BigDecimal;

import edu.sjsu.team10.util.Forms;

// One ingredient a recipe needs more of than the restaurant has.
public class ShortfallLine {

    // what happened when we tried to put this line into the request form
    public static final String PREFILLED = "prefilled";       // restocker carries it, quantity filled in
    public static final String NOT_CARRIED = "notcarried";    // restocker doesn't supply it
    public static final String UNIT_MISMATCH = "unitmismatch"; // restocker sells it in a different unit

    private final int ingId;
    private final String name;
    private final String unit;
    private final BigDecimal needed;
    private final BigDecimal onHand;
    private final BigDecimal shortBy;
    private String outcome = "";

    public ShortfallLine(int ingId, String name, String unit, BigDecimal needed,
                         BigDecimal onHand, BigDecimal shortBy) {
        this.ingId = ingId;
        this.name = name;
        this.unit = unit == null ? "" : unit;
        this.needed = needed;
        this.onHand = onHand;
        this.shortBy = shortBy;
    }

    public int getIngId() { return ingId; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public BigDecimal getShortBy() { return shortBy; }
    public String getNeededDisplay() { return Forms.plain(needed); }
    public String getOnHandDisplay() { return Forms.plain(onHand); }
    public String getShortByDisplay() { return Forms.plain(shortBy); }
    public String getOutcome() { return outcome; }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
}
