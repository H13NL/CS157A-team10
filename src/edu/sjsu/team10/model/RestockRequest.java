package edu.sjsu.team10.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import edu.sjsu.team10.util.Forms;

// A restock request with its items (restock_request + contain rows).
public class RestockRequest {

    // one ingredient line of the request
    public static class Item {
        private final String name;
        private final String unit;
        private final BigDecimal qty;
        private final BigDecimal price; // restocker's current price, null if they no longer carry it

        public Item(String name, String unit, BigDecimal qty, BigDecimal price) {
            this.name = name;
            this.unit = unit == null ? "" : unit;
            this.qty = qty;
            this.price = price;
        }

        public String getName() { return name; }
        public String getUnit() { return unit; }
        public BigDecimal getQty() { return qty; }
        public BigDecimal getPrice() { return price; }
        public String getQtyDisplay() { return Forms.plain(qty); }
    }

    private final int requestId;
    private final String status;
    private final Timestamp createdAt;
    private final Timestamp respondedAt;
    private final String chefName;
    private final String restaurantName;
    private final String restockerName;
    private final String recipeName; // null for a simple request
    private final List<Item> items = new ArrayList<>();

    public RestockRequest(int requestId, String status, Timestamp createdAt, Timestamp respondedAt,
                          String chefName, String restaurantName, String restockerName, String recipeName) {
        this.requestId = requestId;
        this.status = status == null ? "" : status.toLowerCase();
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
        this.chefName = chefName;
        this.restaurantName = restaurantName;
        this.restockerName = restockerName;
        this.recipeName = recipeName;
    }

    public int getRequestId() { return requestId; }
    public String getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getRespondedAt() { return respondedAt; }
    public String getChefName() { return chefName; }
    public String getRestaurantName() { return restaurantName; }
    public String getRestockerName() { return restockerName; }
    public String getRecipeName() { return recipeName; }
    public List<Item> getItems() { return items; }

    public void addItem(Item item) {
        items.add(item);
    }

    // estimated cost at the restocker's current prices
    public BigDecimal getEstimatedTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Item item : items) {
            if (item.getPrice() != null) {
                total = total.add(item.getPrice().multiply(item.getQty()));
            }
        }
        return total;
    }

    public String getEstimatedTotalDisplay() {
        return Forms.money(getEstimatedTotal());
    }
}
