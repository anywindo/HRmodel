package com.hr.dto.leave;

import model.leave.LeaveRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveResponseDTO {
    private String requestId;
    private String employeeId;
    private String employeeName;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
    private String managerApproverName;
    private String hrApproverName;
    private String attachmentUrl;
    
    public LeaveResponseDTO(LeaveRequest request) {
        this.requestId = request.getRequestId();
        this.employeeId = request.getEmployee().getEmployeeId();
        this.employeeName = request.getEmployee().getFullName().getFirstName() + " " + request.getEmployee().getFullName().getLastName();
        this.leaveType = request.getLeaveType().name();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
        this.reason = request.getReason();
        this.status = request.getStatus().name();
        this.createdAt = request.getCreatedAt();
        this.attachmentUrl = request.getAttachmentUrl();
        if (request.getManagerApprover() != null && request.getManagerApprover().getFullName() != null) {
            this.managerApproverName = request.getManagerApprover().getFullName().getFirstName() + " " + request.getManagerApprover().getFullName().getLastName();
        }
        if (request.getHrApprover() != null && request.getHrApprover().getFullName() != null) {
            this.hrApproverName = request.getHrApprover().getFullName().getFirstName() + " " + request.getHrApprover().getFullName().getLastName();
        }
    }

    public LeaveResponseDTO() {}

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getManagerApproverName() { return managerApproverName; }
    public void setManagerApproverName(String managerApproverName) { this.managerApproverName = managerApproverName; }
    public String getHrApproverName() { return hrApproverName; }
    public void setHrApproverName(String hrApproverName) { this.hrApproverName = hrApproverName; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
}
