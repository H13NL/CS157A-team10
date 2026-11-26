package edu.sjsu.team10.model;

// An approved restocker a chef can send a request to.
public class RestockerOption {

    private final int userId;
    private final String companyName;
    private final String contactName;
    private final int catalogSize;

    public RestockerOption(int userId, String companyName, String contactName, int catalogSize) {
        this.userId = userId;
        this.companyName = companyName;
        this.contactName = contactName;
        this.catalogSize = catalogSize;
    }

    public int getUserId() { return userId; }
    public String getContactName() { return contactName; }
    public int getCatalogSize() { return catalogSize; }

    // company name, or the person's name if no company was entered
    public String getCompanyName() {
        return companyName == null || companyName.isEmpty() ? contactName : companyName;
    }
}
