package model.employee;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Salary {

    private final BigDecimal amount;

    public Salary(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Salary is required.");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Salary cannot be negative.");
        }

        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Salary increase(BigDecimal increment) {
        if (increment == null || increment.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid increment.");
        }

        return new Salary(amount.add(increment));
    }

    public Salary decrease(BigDecimal deduction) {
        if (deduction == null || deduction.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid deduction.");
        }

        BigDecimal result = amount.subtract(deduction);

        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Salary cannot be negative.");
        }

        return new Salary(result);
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Salary other)) return false;
        return amount.equals(other.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
}