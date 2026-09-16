package com.hr.controller;

import model.company.Address;
import model.company.CompanyContact;
import model.company.CompanyDetail;
import model.company.CompanyProfile;
import model.employee.Email;
import model.employee.PhoneNumber;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repository.company.CompanyProfileRepository;

@RestController
@RequestMapping("/api/company-profile")
@CrossOrigin(origins = "${cors.allowed.origins}")
public class CompanyProfileController {

    private final CompanyProfileRepository repository;

    public CompanyProfileController(CompanyProfileRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<CompanyProfile> getCompanyProfile() {
        return repository.findFirstByOrderByIdAsc()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record UpdateCompanyProfileRequest(
            String logoUrl,
            String companyName,
            String street,
            String city,
            String state,
            String postalCode,
            String country,
            String phoneCountryCode,
            String phoneNumber,
            String faxCountryCode,
            String faxNumber,
            String email
    ) {}

    @PutMapping
    public ResponseEntity<CompanyProfile> updateCompanyProfile(@RequestBody UpdateCompanyProfileRequest req) {
        Address address = new Address(req.street(), req.city(), req.state(), req.postalCode(), req.country());
        CompanyDetail detail = new CompanyDetail(req.logoUrl(), req.companyName(), address);

        PhoneNumber phone = new PhoneNumber(req.phoneNumber(), req.phoneCountryCode());
        PhoneNumber fax = (req.faxNumber() != null && !req.faxNumber().isBlank())
                ? new PhoneNumber(req.faxNumber(), req.faxCountryCode() != null ? req.faxCountryCode() : req.phoneCountryCode())
                : null;
        Email email = new Email(req.email());

        CompanyContact contact = new CompanyContact(phone, fax, email);

        CompanyProfile profile = repository.findFirstByOrderByIdAsc().orElseGet(() ->
                CompanyProfile.create("COMP-001", detail, contact)
        );

        profile.updateDetail(detail);
        profile.updateContact(contact);

        CompanyProfile saved = repository.save(profile);
        return ResponseEntity.ok(saved);
    }
}
