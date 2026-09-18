package com.hr.controller.onboarding;

import com.hr.dto.onboarding.EmployeeTaskDTO;
import com.hr.dto.onboarding.OnboardingTemplateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import service.onboarding.OnboardingService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN')")
    public ResponseEntity<List<OnboardingTemplateDTO>> getAllTemplates() {
        return ResponseEntity.ok(onboardingService.getAllTemplates());
    }

    @PostMapping("/templates")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN')")
    public ResponseEntity<OnboardingTemplateDTO> createTemplate(@RequestBody OnboardingTemplateDTO dto) {
        return ResponseEntity.ok(onboardingService.createTemplate(dto));
    }

    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> assignTemplate(@RequestParam Long templateId, @RequestParam Long employeeId) {
        onboardingService.assignTemplateToEmployee(templateId, employeeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<EmployeeTaskDTO>> getMyTasks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Find if user has a specific employee ID linked. 
        // For simplicity in this endpoint we will return tasks assigned to this user's ROLES
        // plus tasks assigned to EMPLOYEE if they have an associated employeeId.
        // Assuming user ID can be parsed from principal name or we just use ROLES for now.
        
        List<EmployeeTaskDTO> allTasks = new ArrayList<>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            allTasks.addAll(onboardingService.getTasksByRole(authority.getAuthority()));
        }

        // Ideally we also fetch employee-specific tasks if they are a regular employee:
        // Long employeeId = getUserEmployeeId(authentication);
        // allTasks.addAll(onboardingService.getTasksForEmployee(employeeId));

        return ResponseEntity.ok(allTasks);
    }
    
    @GetMapping("/employee-tasks/{employeeId}")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN')")
    public ResponseEntity<List<EmployeeTaskDTO>> getEmployeeTasks(@PathVariable Long employeeId) {
        return ResponseEntity.ok(onboardingService.getTasksForEmployee(employeeId));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable Long taskId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Just extract a placeholder user ID for now, 
        // normally you'd parse from JWT. Let's use 1L as a dummy or parse if available.
        Long userId = 1L; 
        try {
            userId = Long.parseLong(authentication.getName());
        } catch (Exception e) {}
        
        onboardingService.completeTask(taskId, userId);
        return ResponseEntity.ok().build();
    }
}
