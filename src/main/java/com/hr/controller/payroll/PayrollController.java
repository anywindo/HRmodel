package com.hr.controller.payroll;

import com.hr.dto.payroll.PayrollItemDTO;
import com.hr.dto.payroll.PayrollRunDTO;
import model.payroll.PayrollItem;
import model.payroll.PayrollRun;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import service.payroll.PayrollService;
import service.payroll.PayslipPdfService;
import repository.payroll.PayrollItemRepository;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payroll/runs")
public class PayrollController {

    private final PayrollService payrollService;
    private final PayslipPdfService pdfService;
    private final repository.employee.EmployeeRepository employeeRepository;
    private final PayrollItemRepository payrollItemRepository;

    public PayrollController(PayrollService payrollService, PayslipPdfService pdfService, repository.employee.EmployeeRepository employeeRepository, PayrollItemRepository payrollItemRepository) {
        this.payrollService = payrollService;
        this.pdfService = pdfService;
        this.employeeRepository = employeeRepository;
        this.payrollItemRepository = payrollItemRepository;
    }

    private Long getEmployeeIdOrNull(Authentication authentication) {
        if (authentication == null) return null;
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

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'FINANCE', 'SUPER_ADMIN') or hasAuthority('payroll:view')")
    public ResponseEntity<List<PayrollRunDTO>> getAllRuns() {
        return ResponseEntity.ok(payrollService.getAllRuns().stream()
                .sorted((a, b) -> b.getPeriodEnd().compareTo(a.getPeriodEnd()))
                .map(this::mapToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'FINANCE', 'SUPER_ADMIN') or hasAuthority('payroll:view')")
    public ResponseEntity<PayrollRunDTO> getRun(@PathVariable Long id) {
        return ResponseEntity.ok(mapToDTO(payrollService.getRun(id)));
    }

    @PostMapping("/draft")
    @PreAuthorize("hasAnyRole('HR', 'FINANCE', 'SUPER_ADMIN') or hasAuthority('payroll:run')")
    public ResponseEntity<PayrollRunDTO> generateDraftRun(
            @RequestParam String periodName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            Authentication authentication) {
        Long creatorId = getEmployeeIdOrNull(authentication);
        if (creatorId == null) {
            creatorId = employeeRepository.findAll().stream()
                    .filter(e -> e.getStatus() == model.employee.EmployeeStatus.ACTIVE)
                    .map(model.employee.Employee::getId)
                    .findFirst()
                    .orElse(1L);
        }
        PayrollRun run = payrollService.generateDraftRun(periodName, periodStart, periodEnd, creatorId);
        return ResponseEntity.ok(mapToDTO(run));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('payroll:run')")
    public ResponseEntity<PayrollRunDTO> submitForReview(@PathVariable Long id) {
        return ResponseEntity.ok(mapToDTO(payrollService.submitForReview(id)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('FINANCE', 'SUPER_ADMIN') or hasAuthority('payroll:approve')")
    public ResponseEntity<PayrollRunDTO> approveRun(@PathVariable Long id) {
        return ResponseEntity.ok(mapToDTO(payrollService.approveRun(id)));
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('payroll:run') or hasAuthority('payroll:finalize')")
    public ResponseEntity<PayrollRunDTO> finalizeRun(@PathVariable Long id) {
        return ResponseEntity.ok(mapToDTO(payrollService.finalizeRun(id)));
    }

    @GetMapping("/my-payslips")
    public ResponseEntity<List<PayrollItemDTO>> getMyPayslips(Authentication authentication) {
        Long employeeId = getEmployeeIdOrNull(authentication);
        if (employeeId == null) {
            return ResponseEntity.ok(List.of());
        }
        // Only return items from FINALIZED runs, sorted descending by date
        List<PayrollItemDTO> myItems = payrollService.getAllRuns().stream()
                .filter(run -> run.getStatus() == model.payroll.PayrollStatus.FINALIZED)
                .sorted((a, b) -> b.getPeriodEnd().compareTo(a.getPeriodEnd()))
                .flatMap(run -> run.getItems().stream())
                .filter(item -> item.getEmployee().getId().equals(employeeId))
                .map(item -> {
                    PayrollItemDTO dto = mapItemToDTO(item);
                    dto.setPeriodName(item.getPayrollRun().getPeriodName());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(myItems);
    }

    @GetMapping("/items/{itemId}/pdf")
    public ResponseEntity<byte[]> getPayslipPdf(@PathVariable Long itemId, Authentication authentication) {
        PayrollItem item = payrollService.getPayrollItem(itemId);
        
        Long userId = getEmployeeIdOrNull(authentication);
        boolean isOwner = userId != null && item.getEmployee().getId().equals(userId);
        boolean isHrOrAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR") || 
                              a.getAuthority().equals("ROLE_FINANCE") || 
                              a.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                              a.getAuthority().equals("ROLE_ADMIN") ||
                              a.getAuthority().equals("payroll:view"));

        if (!isOwner && !isHrOrAdmin) {
            return ResponseEntity.status(403).build();
        }
        
        // If owner, they can only see FINALIZED payslips
        if (isOwner && !isHrOrAdmin && item.getPayrollRun().getStatus() != model.payroll.PayrollStatus.FINALIZED) {
            return ResponseEntity.status(403).build();
        }

        // If owner and not HR/Admin, they must acknowledge/sign before downloading the PDF
        if (isOwner && !isHrOrAdmin && !Boolean.TRUE.equals(item.getIsAcknowledged())) {
            return ResponseEntity.status(403).build();
        }

        byte[] pdf = pdfService.generatePayslipPdf(item);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "payslip-" + itemId + ".pdf");
        
        return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);
    }

    @PostMapping("/items/{itemId}/acknowledge")
    public ResponseEntity<PayrollItemDTO> acknowledgePayslip(
            @PathVariable Long itemId,
            HttpServletRequest request,
            Authentication authentication) {
        
        Long userId = getEmployeeIdOrNull(authentication);
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }

        PayrollItem item = payrollService.getPayrollItem(itemId);
        if (!item.getEmployee().getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        if (item.getPayrollRun().getStatus() != model.payroll.PayrollStatus.FINALIZED) {
            return ResponseEntity.badRequest().build();
        }

        item.setIsAcknowledged(true);
        item.setAcknowledgedAt(LocalDateTime.now());
        
        // Get IP address
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        item.setAcknowledgmentIp(ipAddress);

        PayrollItem savedItem = payrollItemRepository.save(item);
        
        PayrollItemDTO dto = mapItemToDTO(savedItem);
        dto.setPeriodName(savedItem.getPayrollRun().getPeriodName());
        
        return ResponseEntity.ok(dto);
    }

    private PayrollRunDTO mapToDTO(PayrollRun run) {
        PayrollRunDTO dto = new PayrollRunDTO();
        dto.setId(run.getId());
        dto.setPeriodName(run.getPeriodName());
        dto.setPeriodStart(run.getPeriodStart());
        dto.setPeriodEnd(run.getPeriodEnd());
        dto.setStatus(run.getStatus());
        dto.setCreatedAt(run.getCreatedAt());
        dto.setCreatedBy(run.getCreatedBy() != null ? run.getCreatedBy().getFullName().getFullName() : "System");
        dto.setTotalGrossPay(run.getTotalGrossPay());
        dto.setTotalNetPay(run.getTotalNetPay());

        if (run.getItems() != null) {
            dto.setItems(run.getItems().stream().map(this::mapItemToDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private PayrollItemDTO mapItemToDTO(PayrollItem item) {
        PayrollItemDTO dto = new PayrollItemDTO();
        dto.setId(item.getId());
        dto.setEmployeeId(item.getEmployee().getId());
        dto.setEmployeeName(item.getEmployee().getFullName().getFullName());
        
        if (item.getEmployee().getPosition() != null) {
            dto.setPosition(item.getEmployee().getPosition().getTitle());
            if (item.getEmployee().getPosition().getDepartment() != null) {
                dto.setDepartment(item.getEmployee().getPosition().getDepartment().getName());
            }
        }

        dto.setBaseSalary(item.getBaseSalary());
        dto.setOvertimePay(item.getOvertimePay());
        dto.setAllowances(item.getAllowances());
        
        dto.setLateDeductions(item.getLateDeductions());
        dto.setAbsenceDeductions(item.getAbsenceDeductions());
        
        dto.setBpjsKesehatanEmployee(item.getBpjsKesehatanEmployee());
        dto.setBpjsKesehatanCompany(item.getBpjsKesehatanCompany());
        dto.setBpjsKetenagakerjaanEmployee(item.getBpjsKetenagakerjaanEmployee());
        dto.setBpjsKetenagakerjaanCompany(item.getBpjsKetenagakerjaanCompany());
        
        dto.setPph21Tax(item.getPph21Tax());
        
        dto.setGrossPay(item.getGrossPay());
        dto.setTotalDeductions(item.getTotalDeductions());
        dto.setNetPay(item.getNetPay());
        
        dto.setIsAcknowledged(item.getIsAcknowledged());
        dto.setAcknowledgedAt(item.getAcknowledgedAt());
        
        return dto;
    }
}
