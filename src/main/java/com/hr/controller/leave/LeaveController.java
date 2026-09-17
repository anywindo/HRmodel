package com.hr.controller.leave;

import com.hr.dto.leave.LeaveBalanceDTO;
import com.hr.dto.leave.LeaveCalendarEventDTO;
import com.hr.dto.leave.LeaveDelegationDTO;
import com.hr.dto.leave.LeaveRequestDTO;
import com.hr.dto.leave.LeaveResponseDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import service.leave.LeaveService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping("/my-balance")
    public ResponseEntity<LeaveBalanceDTO> getMyBalance() {
        return ResponseEntity.ok(leaveService.getMyBalance());
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<LeaveResponseDTO>> getMyRequests() {
        return ResponseEntity.ok(leaveService.getMyRequests());
    }

    @PostMapping("/request")
    public ResponseEntity<LeaveResponseDTO> submitRequest(@RequestBody LeaveRequestDTO request) {
        return ResponseEntity.ok(leaveService.submitRequest(request));
    }

    @GetMapping("/approvals")
    public ResponseEntity<List<LeaveResponseDTO>> getApprovals() {
        return ResponseEntity.ok(leaveService.getApprovalsForMe());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<LeaveResponseDTO> approveRequest(@PathVariable String id) {
        return ResponseEntity.ok(leaveService.approveRequest(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<LeaveResponseDTO> rejectRequest(@PathVariable String id) {
        return ResponseEntity.ok(leaveService.rejectRequest(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<LeaveResponseDTO> cancelRequest(@PathVariable String id) {
        return ResponseEntity.ok(leaveService.cancelRequest(id));
    }

    @GetMapping("/approval-history")
    public ResponseEntity<List<LeaveResponseDTO>> getApprovalHistory() {
        return ResponseEntity.ok(leaveService.getApprovalHistory());
    }

    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN')")
    @PostMapping("/delegate")
    public ResponseEntity<LeaveDelegationDTO> delegateApproval(@RequestBody LeaveDelegationDTO request) {
        return ResponseEntity.ok(leaveService.delegateApproval(request));
    }

    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN')")
    @GetMapping("/delegations")
    public ResponseEntity<List<LeaveDelegationDTO>> getMyDelegations() {
        return ResponseEntity.ok(leaveService.getMyDelegations());
    }

    @GetMapping("/delegations/received")
    public ResponseEntity<List<LeaveDelegationDTO>> getMyReceivedDelegations() {
        return ResponseEntity.ok(leaveService.getMyReceivedDelegations());
    }

    @PreAuthorize("hasAnyRole('HR', 'SUPER_ADMIN')")
    @PostMapping("/delegation/{id}/revoke")
    public ResponseEntity<LeaveDelegationDTO> revokeDelegation(@PathVariable String id) {
        return ResponseEntity.ok(leaveService.revokeDelegation(id));
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<LeaveCalendarEventDTO>> getCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate end) {
        return ResponseEntity.ok(leaveService.getCalendarEvents(start, end));
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadAttachment(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        try {
            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Generate unique file name
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + extension;
            
            Path filePath = uploadDir.resolve(uniqueFileName);
            file.transferTo(filePath.toFile());
            
            return ResponseEntity.ok(Map.of("url", "/uploads/" + uniqueFileName));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        }
    }
}
