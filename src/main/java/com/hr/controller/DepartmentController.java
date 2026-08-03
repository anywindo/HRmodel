package com.hr.controller;

import com.hr.dto.DepartmentRequest;
import com.hr.dto.DepartmentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import service.department.DepartmentService;
import service.employee.EmployeeService;
import com.hr.dto.EmployeeResponse;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "${cors.allowed.origins}")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    public DepartmentController(DepartmentService departmentService, EmployeeService employeeService) {
        this.departmentService = departmentService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<DepartmentResponse> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @GetMapping("/{id}")
    public DepartmentResponse getDepartmentById(@PathVariable String id) {
        return departmentService.getDepartmentById(id);
    }

    @GetMapping("/{id}/employees")
    public List<EmployeeResponse> getEmployeesByDepartmentId(@PathVariable String id) {
        return employeeService.getEmployeesByDepartmentId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse createDepartment(@RequestBody DepartmentRequest request) {
        return departmentService.createDepartment(request);
    }

    @PutMapping("/{id}")
    public DepartmentResponse updateDepartment(@PathVariable String id, @RequestBody DepartmentRequest request) {
        return departmentService.updateDepartment(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDepartment(@PathVariable String id) {
        departmentService.deleteDepartment(id);
    }
}
