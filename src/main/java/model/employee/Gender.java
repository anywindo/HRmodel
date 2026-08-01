package model.employee;

public enum Gender {
    MAN("Man"),
    WOMAN("Woman"),
    NON_BINARY("Non-binary"),
    AGENDER("Agender"),
    GENDERFLUID("Genderfluid"),
    OTHER("Other"),
    PREFER_NOT_TO_SAY("Prefer not to say");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Gender fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Gender is required.");
        }

        return switch (value.trim().toLowerCase()) {
            case "man" -> MAN;
            case "woman" -> WOMAN;
            case "non-binary", "nonbinary" -> NON_BINARY;
            case "agender" -> AGENDER;
            case "genderfluid" -> GENDERFLUID;
            case "other" -> OTHER;
            case "prefer not to say" -> PREFER_NOT_TO_SAY;
            default -> throw new IllegalArgumentException("Invalid gender.");
        };
    }
}