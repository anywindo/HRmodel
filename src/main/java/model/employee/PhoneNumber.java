package model.employee;

import jakarta.persistence.Embeddable;

@Embeddable
public final class PhoneNumber {
    private String phoneNumber;
    private String countryCode;

    protected PhoneNumber() {}

    public PhoneNumber(String phoneNumber, String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            throw new IllegalArgumentException("Country code is required.");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        countryCode = countryCode.trim();
        phoneNumber = phoneNumber.trim().replaceAll("[\\s()-]", "");

        if (!countryCode.matches("^\\+[1-9]\\d{0,3}$")) {
            throw new IllegalArgumentException("Invalid country code.");
        }

        if (!phoneNumber.matches("^\\d{6,15}$")) {
            throw new IllegalArgumentException("Invalid phone number.");
        }

        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFullNumber() {
        return countryCode + phoneNumber;
    }

    @Override
    public String toString() {
        return getFullNumber();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PhoneNumber other)) return false;
        return countryCode.equals(other.countryCode) && phoneNumber.equals(other.phoneNumber);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(countryCode, phoneNumber);
    }
}
