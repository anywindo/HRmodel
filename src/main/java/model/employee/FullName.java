package model.employee;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public final class FullName {

    private String firstName;
    private String middleName;
    private String lastName;

    protected FullName() {}

    public FullName(String firstName, String middleName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required.");
        }

        String combined = (firstName.trim() + " " 
                + (middleName != null && !middleName.isBlank() ? middleName.trim() + " " : "") 
                + lastName.trim()).replaceAll("\\s+", " ");
                
        String[] words = combined.split(" ");
        
        this.firstName = words[0];
        this.lastName = words[words.length - 1];
        
        if (words.length > 2) {
            this.middleName = String.join(" ", java.util.Arrays.copyOfRange(words, 1, words.length - 1));
        } else {
            this.middleName = "";
        }
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