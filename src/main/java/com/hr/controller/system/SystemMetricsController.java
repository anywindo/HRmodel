package com.hr.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repository.employee.EmployeeRepository;
import repository.department.DepartmentRepository;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemMetricsController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // JVM Memory
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("totalMemory", runtime.totalMemory());
        memory.put("freeMemory", runtime.freeMemory());
        memory.put("maxMemory", runtime.maxMemory());
        memory.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        metrics.put("jvmMemory", memory);

        // OS Info
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> osInfo = new HashMap<>();
        osInfo.put("architecture", osBean.getArch());
        osInfo.put("name", osBean.getName());
        osInfo.put("version", osBean.getVersion());
        osInfo.put("availableProcessors", osBean.getAvailableProcessors());
        metrics.put("os", osInfo);

        // Database Summary
        Map<String, Object> dbSummary = new HashMap<>();
        dbSummary.put("totalEmployees", employeeRepository.count());
        dbSummary.put("totalDepartments", departmentRepository.count());
        metrics.put("database", dbSummary);

        return ResponseEntity.ok(metrics);
    }
}
