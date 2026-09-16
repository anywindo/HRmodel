package model.company;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "company_profiles")
public class CompanyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private CompanyDetail detail;

    @Embedded
    private CompanyContact contact;

    protected CompanyProfile() {}

    private CompanyProfile(CompanyDetail detail, CompanyContact contact) {
        if (detail == null) {
            throw new IllegalArgumentException("Company detail is required.");
        }
        if (contact == null) {
            throw new IllegalArgumentException("Company contact is required.");
        }

        this.detail = detail;
        this.contact = contact;
    }

    public static CompanyProfile create(CompanyDetail detail, CompanyContact contact) {
        return new CompanyProfile(detail, contact);
    }

    public void updateDetail(CompanyDetail detail) {
        if (detail == null) {
            throw new IllegalArgumentException("Company detail cannot be null.");
        }
        this.detail = detail;
    }

    public void updateContact(CompanyContact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Company contact cannot be null.");
        }
        this.contact = contact;
    }

    public Long getId() {
        return id;
    }

    public CompanyDetail getDetail() {
        return detail;
    }

    public CompanyContact getContact() {
        return contact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyProfile that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
