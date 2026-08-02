package model.department;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public final class DepartmentId implements Serializable {
    private String value; // Private, not final for JPA

    protected DepartmentId() {
        // JPA requires default constructor
    }

    public DepartmentId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Department ID is required.");
        }
        this.value = value.trim().toUpperCase();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DepartmentId other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
