package model.payroll;

import jakarta.persistence.*;
import model.employee.Employee;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_items")
public class PayrollItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_id", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Income
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal overtimePay = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal allowances = BigDecimal.ZERO;

    // Deductions
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal lateDeductions = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal absenceDeductions = BigDecimal.ZERO;

    // Taxes & BPJS (Company & Employee portion snapshots)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal bpjsKesehatanEmployee = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal bpjsKesehatanCompany = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal bpjsKetenagakerjaanEmployee = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal bpjsKetenagakerjaanCompany = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal pph21Tax = BigDecimal.ZERO;

    // Totals
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal grossPay = BigDecimal.ZERO; // Base + Overtime + Allowances
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO; // Late + Absence + BPJS Employee + PPh21
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netPay = BigDecimal.ZERO; // Gross - Total Deductions

    // Acknowledgment
    @Column(name = "is_acknowledged", nullable = false)
    private Boolean isAcknowledged = false;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledgment_ip")
    private String acknowledgmentIp;

    public PayrollItem() {}

    public PayrollItem(Employee employee) {
        this.employee = employee;
    }

    public Long getId() { return id; }
    
    public PayrollRun getPayrollRun() { return payrollRun; }
    public void setPayrollRun(PayrollRun payrollRun) { this.payrollRun = payrollRun; }
    
    public Employee getEmployee() { return employee; }
    
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
    
    public void calculateTotals() {
        this.grossPay = baseSalary.add(overtimePay).add(allowances);
        
        // Sum of all employee deductions
        this.totalDeductions = lateDeductions
                .add(absenceDeductions)
                .add(bpjsKesehatanEmployee)
                .add(bpjsKetenagakerjaanEmployee)
                .add(pph21Tax);
                
        this.netPay = this.grossPay.subtract(this.totalDeductions);
    }
    
    public Boolean getIsAcknowledged() { return isAcknowledged; }
    public void setIsAcknowledged(Boolean isAcknowledged) { this.isAcknowledged = isAcknowledged; }

    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public String getAcknowledgmentIp() { return acknowledgmentIp; }
    public void setAcknowledgmentIp(String acknowledgmentIp) { this.acknowledgmentIp = acknowledgmentIp; }
}
