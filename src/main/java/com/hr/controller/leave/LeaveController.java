package com.hr.controller.leave;

import com.hr.dto.leave.LeaveBalanceDTO;
import com.hr.dto.leave.LeaveRequestDTO;
import com.hr.dto.leave.LeaveResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import service.leave.LeaveService;

import java.util.List;

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

    @GetMapping("/approval-history")
    public ResponseEntity<List<LeaveResponseDTO>> getApprovalHistory() {
        return ResponseEntity.ok(leaveService.getApprovalHistory());
    }
}
