package model.employee;

import java.util.Objects;

public final class FullName {

    private final String firstName;
    private final String middleName;
    private final String lastName;

    public FullName(String firstName, String middleName, String lastName) {
        this.firstName = validateName(firstName, "First name");
        this.middleName = validateOptionalName(middleName);
        this.lastName = validateName(lastName, "Last name");
    }

    private String validateName(String name, String fieldName) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }

        name = name.trim();

        if (name.contains(" ")) {
            throw new IllegalArgumentException(fieldName + " cannot contain spaces.");
        }

        return name;
    }

    private String validateOptionalName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }

        name = name.trim();

        if (name.contains(" ")) {
            throw new IllegalArgumentException("Middle name cannot contain spaces.");
        }

        return name;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        if (middleName.isEmpty()) {
            return firstName + " " + lastName;
        }
        return firstName + " " + middleName + " " + lastName;
    }

    public String getInitials() {
        if (middleName.isEmpty()) {
            return "" + firstName.charAt(0) + lastName.charAt(0);
        }
        return "" + firstName.charAt(0) + middleName.charAt(0) + lastName.charAt(0);
    }

    @Override
    public String toString() {
        return getFullName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FullName other)) {
            return false;
        }

        return firstName.equals(other.firstName)
                && middleName.equals(other.middleName)
                && lastName.equals(other.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, middleName, lastName);
    }
}