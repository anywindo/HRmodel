package com.hr.dto.leave;

import model.leave.EmployeeLeaveBalance;

public class LeaveBalanceDTO {
    private String employeeId;
    private double annualLeaveDays;
    private double sickLeaveDays;
    
    public LeaveBalanceDTO(EmployeeLeaveBalance balance) {
        this.employeeId = balance.getEmployee().getEmployeeId();
        this.annualLeaveDays = balance.getAnnualLeaveDays();
        this.sickLeaveDays = balance.getSickLeaveDays();
    }

    public LeaveBalanceDTO() {}

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public double getAnnualLeaveDays() { return annualLeaveDays; }
    public void setAnnualLeaveDays(double annualLeaveDays) { this.annualLeaveDays = annualLeaveDays; }
    public double getSickLeaveDays() { return sickLeaveDays; }
    public void setSickLeaveDays(double sickLeaveDays) { this.sickLeaveDays = sickLeaveDays; }
}
