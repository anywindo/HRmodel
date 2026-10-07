package com.hr.controller.performance;

import com.hr.dto.performance.KpiDTO;
import com.hr.dto.performance.PerformanceReviewDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import service.performance.PerformanceService;

import java.util.List;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final repository.employee.EmployeeRepository employeeRepository;

    public PerformanceController(PerformanceService performanceService, repository.employee.EmployeeRepository employeeRepository) {
        this.performanceService = performanceService;
        this.employeeRepository = employeeRepository;
    }

    private Long getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return employeeRepository.findByEmail_Value(username)
                .map(model.employee.Employee::getId)
                .orElse(null);
    }

    // KPIs

    @GetMapping("/kpis/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<KpiDTO>> getKpis(@PathVariable Long employeeId, Authentication authentication) {
        Long currentUserId = getUserId(authentication);
        boolean isSelf = currentUserId != null && currentUserId.equals(employeeId);
        boolean hasAccess = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("performance:view"));
                
        if (!isSelf && !hasAccess) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(performanceService.getKpisByEmployee(employeeId));
    }

    @PostMapping("/kpis")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('performance:manage')")
    public ResponseEntity<KpiDTO> createKpi(@RequestBody KpiDTO dto) {
        return ResponseEntity.ok(performanceService.createKpi(dto));
    }

    @PutMapping("/kpis/{id}")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('performance:manage')")
    public ResponseEntity<KpiDTO> updateKpi(@PathVariable Long id, @RequestBody KpiDTO dto) {
        return ResponseEntity.ok(performanceService.updateKpi(id, dto));
    }

    @DeleteMapping("/kpis/{id}")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('performance:manage')")
    public ResponseEntity<Void> deleteKpi(@PathVariable Long id) {
        performanceService.deleteKpi(id);
        return ResponseEntity.ok().build();
    }

    // Reviews

    @GetMapping("/reviews/{employeeId}")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN', 'MANAGER') or hasAuthority('performance:view')")
    public ResponseEntity<List<PerformanceReviewDTO>> getReviews(@PathVariable Long employeeId, Authentication authentication) {
        Long currentUserId = getUserId(authentication);
        boolean isSelf = currentUserId != null && currentUserId.equals(employeeId);
        boolean hasAccess = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("performance:view"));
                
        if (!isSelf && !hasAccess) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(performanceService.getReviewsByEmployee(employeeId));
    }

    @PostMapping("/reviews")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('performance:manage')")
    public ResponseEntity<PerformanceReviewDTO> createReview(@RequestBody PerformanceReviewDTO dto) {
        return ResponseEntity.ok(performanceService.createReview(dto));
    }

    @PutMapping("/reviews/{id}")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('performance:manage')")
    public ResponseEntity<PerformanceReviewDTO> updateReview(@PathVariable Long id, @RequestBody PerformanceReviewDTO dto) {
        return ResponseEntity.ok(performanceService.updateReview(id, dto));
    }

    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('performance:manage')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        performanceService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
}
