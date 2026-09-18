package com.hr.dto.attendance;

import model.attendance.OvertimeRequestStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OvertimeResponseDTO {
    private Long id;
    private String employeeId;
    private String employeeName;
    private LocalDate date;
    private BigDecimal hoursRequested;
    private String reason;
    private OvertimeRequestStatus status;
    private LocalDateTime createdAt;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public BigDecimal getHoursRequested() { return hoursRequested; }
    public void setHoursRequested(BigDecimal hoursRequested) { this.hoursRequested = hoursRequested; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public OvertimeRequestStatus getStatus() { return status; }
    public void setStatus(OvertimeRequestStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
