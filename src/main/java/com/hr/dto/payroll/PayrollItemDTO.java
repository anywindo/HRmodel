package com.hr.dto.payroll;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayrollItemDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String department;
    private String position;
    private String periodName;

    private BigDecimal baseSalary;
    private BigDecimal overtimePay;
    private BigDecimal allowances;
    
    private BigDecimal lateDeductions;
    private BigDecimal absenceDeductions;
    
    private BigDecimal bpjsKesehatanEmployee;
    private BigDecimal bpjsKesehatanCompany;
    private BigDecimal bpjsKetenagakerjaanEmployee;
    private BigDecimal bpjsKetenagakerjaanCompany;
    
    private BigDecimal pph21Tax;
    
    private BigDecimal grossPay;
    private BigDecimal totalDeductions;
    private BigDecimal netPay;
    
    private Boolean isAcknowledged;
    private LocalDateTime acknowledgedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) {
        this.position = position;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public BigDecimal getBaseSalary() { return baseSalary; }
    public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
    public BigDecimal getOvertimePay() { return overtimePay; }
    public void setOvertimePay(BigDecimal overtimePay) { this.overtimePay = overtimePay; }
    public BigDecimal getAllowances() { return allowances; }
    public void setAllowances(BigDecimal allowances) { this.allowances = allowances; }
    public BigDecimal getLateDeductions() { return lateDeductions; }
    public void setLateDeductions(BigDecimal lateDeductions) { this.lateDeductions = lateDeductions; }
    public BigDecimal getAbsenceDeductions() { return absenceDeductions; }
    public void setAbsenceDeductions(BigDecimal absenceDeductions) { this.absenceDeductions = absenceDeductions; }
    public BigDecimal getBpjsKesehatanEmployee() { return bpjsKesehatanEmployee; }
    public void setBpjsKesehatanEmployee(BigDecimal bpjsKesehatanEmployee) { this.bpjsKesehatanEmployee = bpjsKesehatanEmployee; }
    public BigDecimal getBpjsKesehatanCompany() { return bpjsKesehatanCompany; }
    public void setBpjsKesehatanCompany(BigDecimal bpjsKesehatanCompany) { this.bpjsKesehatanCompany = bpjsKesehatanCompany; }
    public BigDecimal getBpjsKetenagakerjaanEmployee() { return bpjsKetenagakerjaanEmployee; }
    public void setBpjsKetenagakerjaanEmployee(BigDecimal bpjsKetenagakerjaanEmployee) { this.bpjsKetenagakerjaanEmployee = bpjsKetenagakerjaanEmployee; }
    public BigDecimal getBpjsKetenagakerjaanCompany() { return bpjsKetenagakerjaanCompany; }
    public void setBpjsKetenagakerjaanCompany(BigDecimal bpjsKetenagakerjaanCompany) { this.bpjsKetenagakerjaanCompany = bpjsKetenagakerjaanCompany; }
    public BigDecimal getPph21Tax() { return pph21Tax; }
    public void setPph21Tax(BigDecimal pph21Tax) { this.pph21Tax = pph21Tax; }
    public BigDecimal getGrossPay() { return grossPay; }
    public void setGrossPay(BigDecimal grossPay) { this.grossPay = grossPay; }
    public BigDecimal getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }
    public BigDecimal getNetPay() { return netPay; }
    public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }
    
    public Boolean getIsAcknowledged() { return isAcknowledged; }
    public void setIsAcknowledged(Boolean isAcknowledged) { this.isAcknowledged = isAcknowledged; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
}
