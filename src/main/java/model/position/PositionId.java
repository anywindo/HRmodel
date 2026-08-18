package model.position;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PositionId implements Serializable {
    
    private String value;

    protected PositionId() {} // JPA requires no-arg constructor

    public PositionId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Position ID cannot be null or empty");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PositionId)) return false;
        PositionId that = (PositionId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
