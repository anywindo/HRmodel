package com.hr.dto.payroll;

import model.payroll.PayrollStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PayrollRunDTO {
    private Long id;
    private String periodName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private PayrollStatus status;
    private LocalDate createdAt;
    private String createdBy;
    private BigDecimal totalGrossPay;
    private BigDecimal totalNetPay;
    private List<PayrollItemDTO> items;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPeriodName() { return periodName; }
    public void setPeriodName(String periodName) { this.periodName = periodName; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public PayrollStatus getStatus() { return status; }
    public void setStatus(PayrollStatus status) { this.status = status; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public BigDecimal getTotalGrossPay() { return totalGrossPay; }
    public void setTotalGrossPay(BigDecimal totalGrossPay) { this.totalGrossPay = totalGrossPay; }
    public BigDecimal getTotalNetPay() { return totalNetPay; }
    public void setTotalNetPay(BigDecimal totalNetPay) { this.totalNetPay = totalNetPay; }
    public List<PayrollItemDTO> getItems() { return items; }
    public void setItems(List<PayrollItemDTO> items) { this.items = items; }
}
