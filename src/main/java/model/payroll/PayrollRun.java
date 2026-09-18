package model.payroll;

import jakarta.persistence.*;
import model.employee.Employee;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payroll_runs")
public class PayrollRun {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String periodName; // e.g., "September 2026"

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32, columnDefinition = "VARCHAR(32)")
    private PayrollStatus status = PayrollStatus.DRAFT;

    @OneToMany(mappedBy = "payrollRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollItem> items = new ArrayList<>();

    // Audit fields
    private LocalDate createdAt = LocalDate.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private Employee createdBy;

    public PayrollRun() {}

    public PayrollRun(String periodName, LocalDate periodStart, LocalDate periodEnd, Employee createdBy) {
        this.periodName = periodName;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public String getPeriodName() { return periodName; }
    public void setPeriodName(String periodName) { this.periodName = periodName; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public PayrollStatus getStatus() { return status; }
    public void setStatus(PayrollStatus status) { this.status = status; }
    public List<PayrollItem> getItems() { return items; }
    
    public void addItem(PayrollItem item) {
        items.add(item);
        item.setPayrollRun(this);
    }
    
    public LocalDate getCreatedAt() { return createdAt; }
    public Employee getCreatedBy() { return createdBy; }
    
    public BigDecimal getTotalGrossPay() {
        return items.stream().map(PayrollItem::getGrossPay).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalNetPay() {
        return items.stream().map(PayrollItem::getNetPay).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
