package model.company;

import jakarta.persistence.Embedded;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public final class CompanyDetail {

    private String logoUrl;
    private String companyName;

    @Embedded
    private Address address;

    protected CompanyDetail() {}

    public CompanyDetail(String logoUrl, String companyName, Address address) {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name is required.");
        }
        if (address == null) {
            throw new IllegalArgumentException("Address is required.");
        }

        this.logoUrl = logoUrl != null ? logoUrl.trim() : "";
        this.companyName = companyName.trim();
        this.address = address;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyDetail detail)) return false;
        return Objects.equals(logoUrl, detail.logoUrl) &&
               Objects.equals(companyName, detail.companyName) &&
               Objects.equals(address, detail.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logoUrl, companyName, address);
    }
}
