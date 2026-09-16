package model.company;

import jakarta.persistence.Column;
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

    @Column(name = "profile_id", nullable = false, unique = true)
    private String profileId;

    @Embedded
    private CompanyDetail detail;

    @Embedded
    private CompanyContact contact;

    protected CompanyProfile() {}

    private CompanyProfile(String profileId, CompanyDetail detail, CompanyContact contact) {
        if (profileId == null || profileId.isBlank()) {
            throw new IllegalArgumentException("Profile ID is required.");
        }
        if (detail == null) {
            throw new IllegalArgumentException("Company detail is required.");
        }
        if (contact == null) {
            throw new IllegalArgumentException("Company contact is required.");
        }

        this.profileId = profileId.trim();
        this.detail = detail;
        this.contact = contact;
    }

    public static CompanyProfile create(String profileId, CompanyDetail detail, CompanyContact contact) {
        return new CompanyProfile(profileId, detail, contact);
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

    public String getProfileId() {
        return profileId;
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
        return Objects.equals(profileId, that.profileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId);
    }
}
