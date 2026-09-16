package model.company;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import model.employee.Email;
import model.employee.PhoneNumber;
import java.util.Objects;

@Embeddable
public final class CompanyContact {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "phoneNumber", column = @Column(name = "phone_number")),
        @AttributeOverride(name = "countryCode", column = @Column(name = "phone_country_code"))
    })
    private PhoneNumber phone;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "phoneNumber", column = @Column(name = "fax_number")),
        @AttributeOverride(name = "countryCode", column = @Column(name = "fax_country_code"))
    })
    private PhoneNumber fax;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "email", column = @Column(name = "company_email"))
    })
    private Email email;

    protected CompanyContact() {}

    public CompanyContact(PhoneNumber phone, PhoneNumber fax, Email email) {
        if (phone == null) {
            throw new IllegalArgumentException("Phone is required.");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email is required.");
        }

        this.phone = phone;
        this.fax = fax; // Optional
        this.email = email;
    }

    public PhoneNumber getPhone() {
        return phone;
    }

    public PhoneNumber getFax() {
        return fax;
    }

    public Email getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyContact contact)) return false;
        return Objects.equals(phone, contact.phone) &&
               Objects.equals(fax, contact.fax) &&
               Objects.equals(email, contact.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phone, fax, email);
    }
}
