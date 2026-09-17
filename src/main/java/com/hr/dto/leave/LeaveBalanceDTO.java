package com.hr.dto.leave;

import model.leave.EmployeeLeaveBalance;

public class LeaveBalanceDTO {
    private String employeeId;
    private int annualLeaveDays;
    private int sickLeaveDays;
    
    public LeaveBalanceDTO(EmployeeLeaveBalance balance) {
        this.employeeId = balance.getEmployee().getEmployeeId();
        this.annualLeaveDays = balance.getAnnualLeaveDays();
        this.sickLeaveDays = balance.getSickLeaveDays();
    }

    public LeaveBalanceDTO() {}

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public int getAnnualLeaveDays() { return annualLeaveDays; }
    public void setAnnualLeaveDays(int annualLeaveDays) { this.annualLeaveDays = annualLeaveDays; }
    public int getSickLeaveDays() { return sickLeaveDays; }
    public void setSickLeaveDays(int sickLeaveDays) { this.sickLeaveDays = sickLeaveDays; }
}
