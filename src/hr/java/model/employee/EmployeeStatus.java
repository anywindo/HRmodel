package model.employee;

public enum EmployeeStatus {
    ACTIVE("Active"),
    ON_LEAVE("On Leave"),
    SUSPENDED("Suspended"),
    RESIGNED("Resigned"),
    TERMINATED("Terminated"),
    RETIRED("Retired");

    private final String displayName;

    EmployeeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static EmployeeStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Employee status is required.");
        }

        return switch (value.trim().toLowerCase()) {
            case "active" -> ACTIVE;
            case "on leave", "on_leave" -> ON_LEAVE;
            case "suspended" -> SUSPENDED;
            case "resigned" -> RESIGNED;
            case "terminated" -> TERMINATED;
            case "retired" -> RETIRED;
            default -> throw new IllegalArgumentException("Invalid employee status.");
        };
    }
}
