package com.hr.controller.attendance;

import com.hr.dto.attendance.AttendanceRecordDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import service.attendance.AttendanceService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final repository.employee.EmployeeRepository employeeRepository;

    public AttendanceController(AttendanceService attendanceService, repository.employee.EmployeeRepository employeeRepository) {
        this.attendanceService = attendanceService;
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
        model.employee.Employee employee = employeeRepository.findByEmail_Value(username)
                .orElseThrow(() -> new RuntimeException("Employee not found for username: " + username));
        return employee.getId();
    }

    @GetMapping("/my")
    public ResponseEntity<List<AttendanceRecordDTO>> getMyAttendance(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long employeeId = getUserId(authentication);
        return ResponseEntity.ok(attendanceService.getMyAttendance(employeeId, startDate, endDate));
    }

    @GetMapping("/office-hours")
    public ResponseEntity<model.company.OfficeSettings> getOfficeHours() {
        return ResponseEntity.ok(attendanceService.getOfficeHours());
    }

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceRecordDTO> checkIn(Authentication authentication) {
        Long employeeId = getUserId(authentication);
        return ResponseEntity.ok(attendanceService.checkIn(employeeId, LocalTime.now()));
    }

    @PostMapping("/check-out")
    public ResponseEntity<AttendanceRecordDTO> checkOut(Authentication authentication) {
        Long employeeId = getUserId(authentication);
        return ResponseEntity.ok(attendanceService.checkOut(employeeId, LocalTime.now()));
    }

    // HR / Admin endpoints
    
    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('attendance:view')")
    public ResponseEntity<List<AttendanceRecordDTO>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('attendance:manage')")
    public ResponseEntity<AttendanceRecordDTO> addAttendance(@RequestBody AttendanceRecordDTO dto) {
        return ResponseEntity.ok(attendanceService.addAttendance(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('attendance:manage')")
    public ResponseEntity<AttendanceRecordDTO> updateAttendance(@PathVariable Long id, @RequestBody AttendanceRecordDTO dto) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, dto));
    }
}
