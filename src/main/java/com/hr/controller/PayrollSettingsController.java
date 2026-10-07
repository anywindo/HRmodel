package com.hr.controller;

import com.hr.dto.payroll.PayrollSettingsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import service.payroll.PayrollSettingsService;

@RestController
@RequestMapping("/api/payroll/settings")
public class PayrollSettingsController {

    private final PayrollSettingsService payrollSettingsService;

    public PayrollSettingsController(PayrollSettingsService payrollSettingsService) {
        this.payrollSettingsService = payrollSettingsService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN', 'FINANCE') or hasAuthority('payroll:view') or hasAuthority('payroll:run')")
    public ResponseEntity<PayrollSettingsDTO> getSettings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("USER TRYING TO ACCESS GET /api/payroll/settings: " + (auth != null ? auth.getName() : "null"));
        if (auth != null) {
            System.out.println("AUTHORITIES: " + auth.getAuthorities());
        }
        return ResponseEntity.ok(payrollSettingsService.getSettings());
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('payroll:run')")
    public ResponseEntity<PayrollSettingsDTO> updateSettings(@RequestBody PayrollSettingsDTO dto) {
        return ResponseEntity.ok(payrollSettingsService.updateSettings(dto));
    }
}
