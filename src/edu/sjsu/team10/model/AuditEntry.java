package edu.sjsu.team10.model;

import java.sql.Timestamp;

// One row of audit_log, joined with who did it and (when known) the name of what it touched.
public class AuditEntry {

    private final int logId;
    private final int userId;
    private final String userName;
    private final String userEmail;
    private final String action;
    private final String entityType;
    private final int entityId;
    private final String targetName;
    private final Timestamp createdAt;

    public AuditEntry(int logId, int userId, String userName, String userEmail, String action,
                      String entityType, int entityId, String targetName, Timestamp createdAt) {
        this.logId = logId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.targetName = targetName;
        this.createdAt = createdAt;
    }

    public int getLogId() { return logId; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public int getEntityId() { return entityId; }
    public String getTargetName() { return targetName; }
    public Timestamp getCreatedAt() { return createdAt; }

    // "APPROVE_RESTOCKER" -> "Approve restocker"
    public String getActionLabel() {
        if (action == null || action.isEmpty()) {
            return "";
        }
        String text = action.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    // CSS class for the action badge
    public String getActionTone() {
        if (action == null) {
            return "neutral";
        }
        if (action.startsWith("APPROVE") || action.startsWith("ENABLE")) {
            return "good";
        }
        if (action.startsWith("REJECT") || action.startsWith("DISABLE")) {
            return "bad";
        }
        return "neutral";
    }

    // "restocker #5 · Fresh Farms Supply"
    public String getTargetLabel() {
        String label = entityType + " #" + entityId;
        return targetName == null ? label : label + " · " + targetName;
    }
}
