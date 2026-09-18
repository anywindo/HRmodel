package com.hr.controller.attendance;

import com.hr.dto.attendance.OvertimeRequestDTO;
import com.hr.dto.attendance.OvertimeResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import service.attendance.OvertimeService;

import java.util.List;

import repository.employee.EmployeeRepository;

@RestController
@RequestMapping("/api/overtime")
public class OvertimeController {

    @Autowired
    private OvertimeService overtimeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    private String getEmpId(Authentication authentication) {
        String email = authentication.getName();
        model.employee.Employee employee = employeeRepository.findByEmail_Value(email)
                .orElseThrow(() -> new RuntimeException("Employee not found for email: " + email));
        return employee.getEmployeeId();
    }

    @PostMapping
    public ResponseEntity<OvertimeResponseDTO> createRequest(Authentication authentication, @RequestBody OvertimeRequestDTO dto) {
        String employeeId = getEmpId(authentication);
        return ResponseEntity.ok(overtimeService.submitRequest(employeeId, dto));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OvertimeResponseDTO>> getMyRequests(Authentication authentication) {
        String employeeId = getEmpId(authentication);
        return ResponseEntity.ok(overtimeService.getMyRequests(employeeId));
    }

    @GetMapping("/approvals/manager")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('leave:approve') or hasAuthority('attendance:view') or hasAuthority('attendance:manage')")
    public ResponseEntity<List<OvertimeResponseDTO>> getManagerPendingRequests(Authentication authentication) {
        String managerId = getEmpId(authentication);
        return ResponseEntity.ok(overtimeService.getPendingForManager(managerId));
    }

    @GetMapping("/approvals/hr")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('attendance:manage') or hasAuthority('payroll:approve')")
    public ResponseEntity<List<OvertimeResponseDTO>> getHRPendingRequests() {
        return ResponseEntity.ok(overtimeService.getPendingForHR());
    }

    @PostMapping("/{id}/approve/manager")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('leave:approve') or hasAuthority('attendance:view') or hasAuthority('attendance:manage')")
    public ResponseEntity<OvertimeResponseDTO> approveByManager(@PathVariable Long id, Authentication authentication) {
        String managerId = getEmpId(authentication);
        return ResponseEntity.ok(overtimeService.approveByManager(id, managerId));
    }

    @PostMapping("/{id}/approve/hr")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('attendance:manage') or hasAuthority('payroll:approve')")
    public ResponseEntity<OvertimeResponseDTO> approveByHR(@PathVariable Long id, Authentication authentication) {
        String hrId = getEmpId(authentication);
        return ResponseEntity.ok(overtimeService.approveByHR(id, hrId));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('leave:approve') or hasAuthority('attendance:view') or hasAuthority('attendance:manage')")
    public ResponseEntity<OvertimeResponseDTO> rejectRequest(@PathVariable Long id, Authentication authentication) {
        String rejectedById = getEmpId(authentication);
        return ResponseEntity.ok(overtimeService.rejectRequest(id, rejectedById));
    }

}
