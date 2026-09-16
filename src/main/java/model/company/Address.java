package model.company;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public final class Address {

    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    protected Address() {}

    public Address(String street, String city, String state, String postalCode, String country) {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street is required.");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required.");
        }
        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("Postal code is required.");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country is required.");
        }

        this.street = street.trim();
        this.city = city.trim();
        this.state = state != null ? state.trim() : "";
        this.postalCode = postalCode.trim();
        this.country = country.trim();
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s %s, %s", street, city, state, postalCode, country);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;
        return Objects.equals(street, address.street) &&
               Objects.equals(city, address.city) &&
               Objects.equals(state, address.state) &&
               Objects.equals(postalCode, address.postalCode) &&
               Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, postalCode, country);
    }
}
