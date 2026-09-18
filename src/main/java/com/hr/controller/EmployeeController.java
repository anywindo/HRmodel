package com.hr.controller;

import com.hr.dto.EmployeeRequest;
import com.hr.dto.EmployeeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import service.employee.EmployeeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import repository.employee.EmployeeRepository;
import model.employee.Employee;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "${cors.allowed.origins}") // Allow React Frontend via env property
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeService employeeService, EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR', 'EXECUTIVE', 'FINANCE') or hasAuthority('employee:view')")
    public List<EmployeeResponse> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyEmployeeRecord() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Employee employee = employeeRepository.findByEmail_Value(userDetails.getUsername()).orElseThrow();
        
        return ResponseEntity.ok(employeeService.getEmployeeById(employee.getEmployeeId()));
    }

    @GetMapping("/my-team")
    public List<EmployeeResponse> getMyTeam() {
        return employeeService.getMyTeam();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR', 'EXECUTIVE', 'FINANCE') or hasAuthority('employee:view')")
    public EmployeeResponse getEmployeeById(@PathVariable String id) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR') or hasAuthority('employee:create')")
    public EmployeeResponse createEmployee(@RequestBody EmployeeRequest request) {
        return employeeService.createEmployee(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR') or hasAuthority('employee:edit')")
    public EmployeeResponse updateEmployee(@PathVariable String id, @RequestBody EmployeeRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR') or hasAuthority('employee:edit')")
    public void changeEmployeeStatus(@PathVariable String id, @RequestParam String status, @RequestParam String reason) {
        employeeService.changeEmployeeStatus(id, status, reason);
    }
}
