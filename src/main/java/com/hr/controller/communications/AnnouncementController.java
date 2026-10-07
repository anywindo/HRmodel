package com.hr.controller.communications;

import com.hr.dto.communications.AnnouncementDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import service.communications.AnnouncementService;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final repository.employee.EmployeeRepository employeeRepository;

    public AnnouncementController(AnnouncementService announcementService, repository.employee.EmployeeRepository employeeRepository) {
        this.announcementService = announcementService;
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

    @GetMapping
    public ResponseEntity<List<AnnouncementDTO>> getAnnouncements(Authentication authentication) {
        Long employeeId = getUserId(authentication);
        return ResponseEntity.ok(announcementService.getAllAnnouncements(employeeId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('announcements:manage')")
    public ResponseEntity<AnnouncementDTO> createAnnouncement(Authentication authentication, @RequestBody AnnouncementDTO dto) {
        Long authorId = getUserId(authentication);
        return ResponseEntity.ok(announcementService.createAnnouncement(dto, authorId));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN') or hasAuthority('announcements:manage')")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.ok().build();
    }
}
