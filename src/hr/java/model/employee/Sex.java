package model.employee;

public enum Sex {
    MALE("Male"),
    FEMALE("Female"),
    UNSPECIFIED("Unspecified");

    private final String displayName;

    Sex(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Sex fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Sex is required.");
        }

        return switch (value.trim().toLowerCase()) {
            case "male", "m" -> MALE;
            case "female", "f" -> FEMALE;
            case "unspecified", "unknown", "u" -> UNSPECIFIED;
            default -> throw new IllegalArgumentException("Invalid sex.");
        };
    }
}