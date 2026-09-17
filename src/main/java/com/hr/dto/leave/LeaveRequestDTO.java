package com.hr.dto.leave;

import java.time.LocalDate;

public class LeaveRequestDTO {
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;

    public LeaveRequestDTO() {}

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public java.time.LocalDate getStartDate() { return startDate; }
    public void setStartDate(java.time.LocalDate startDate) { this.startDate = startDate; }
    public java.time.LocalDate getEndDate() { return endDate; }
    public void setEndDate(java.time.LocalDate endDate) { this.endDate = endDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
