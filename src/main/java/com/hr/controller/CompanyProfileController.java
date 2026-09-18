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

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/company-profile")
@CrossOrigin(origins = "${cors.allowed.origins}")
public class CompanyProfileController {

    private final CompanyProfileRepository repository;

    public CompanyProfileController(CompanyProfileRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR', 'EXECUTIVE', 'FINANCE', 'STANDARD_USER') or hasAuthority('company_profile:view')")
    public ResponseEntity<CompanyProfile> getCompanyProfile() {
        return repository.findFirstByOrderByIdAsc()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/logo")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR') or hasAuthority('company_profile:edit')")
    public ResponseEntity<java.util.Map<String, String>> uploadLogo(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "File is empty"));
        }
        try {
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "logo.png";
            String filename = "logo_" + System.currentTimeMillis() + "_" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads");
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            java.nio.file.Path filePath = uploadPath.resolve(filename);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String logoUrl = "http://localhost:8080/uploads/" + filename;
            return ResponseEntity.ok(java.util.Map.of("logoUrl", logoUrl));
        } catch (java.io.IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<java.util.Map<String, String>> handleMaxSizeException(org.springframework.web.multipart.MaxUploadSizeExceededException exc) {
        return ResponseEntity.badRequest().body(java.util.Map.of("error", "File size exceeds maximum limit of 10MB."));
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR') or hasAuthority('company_profile:edit')")
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
                CompanyProfile.create(detail, contact)
        );

        profile.updateDetail(detail);
        profile.updateContact(contact);

        CompanyProfile saved = repository.save(profile);
        return ResponseEntity.ok(saved);
    }
}
