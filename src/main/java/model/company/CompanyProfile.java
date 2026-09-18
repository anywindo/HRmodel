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

    @Embedded
    private OfficeSettings officeSettings;

    protected CompanyProfile() {}

    private CompanyProfile(CompanyDetail detail, CompanyContact contact, OfficeSettings officeSettings) {
        if (detail == null) {
            throw new IllegalArgumentException("Company detail is required.");
        }
        if (contact == null) {
            throw new IllegalArgumentException("Company contact is required.");
        }

        this.detail = detail;
        this.contact = contact;
        this.officeSettings = officeSettings != null ? officeSettings : OfficeSettings.defaultSettings();
    }

    public static CompanyProfile create(CompanyDetail detail, CompanyContact contact) {
        return new CompanyProfile(detail, contact, OfficeSettings.defaultSettings());
    }

    public static CompanyProfile create(CompanyDetail detail, CompanyContact contact, OfficeSettings officeSettings) {
        return new CompanyProfile(detail, contact, officeSettings);
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

    public void updateOfficeSettings(OfficeSettings officeSettings) {
        if (officeSettings == null) {
            throw new IllegalArgumentException("Office settings cannot be null.");
        }
        this.officeSettings = officeSettings;
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

    public OfficeSettings getOfficeSettings() {
        return officeSettings != null ? officeSettings : OfficeSettings.defaultSettings();
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
