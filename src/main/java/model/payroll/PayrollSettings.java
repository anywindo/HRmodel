package model.payroll;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payroll_settings")
public class PayrollSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int workingDaysPerMonth = 21;

    // Overtime multipliers
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal overtimeMultiplierFirstHour = new BigDecimal("1.50");

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal overtimeMultiplierSubsequentHours = new BigDecimal("2.00");

    // BPJS Ketenagakerjaan rates (employee side)
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal bpjsJhtEmployeeRate = new BigDecimal("0.0200"); // 2%

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal bpjsJpEmployeeRate = new BigDecimal("0.0100"); // 1%

    // BPJS Ketenagakerjaan rates (employer side)
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal bpjsJhtEmployerRate = new BigDecimal("0.0370"); // 3.7%

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal bpjsJpEmployerRate = new BigDecimal("0.0200"); // 2%

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal bpjsJkkRate = new BigDecimal("0.0024"); // 0.24%

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal bpjsJkmRate = new BigDecimal("0.0030"); // 0.3%
    
    // BPJS Kesehatan rates
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal bpjsKesehatanEmployeeRate = new BigDecimal("0.0100"); // 1%
    
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal bpjsKesehatanCompanyRate = new BigDecimal("0.0400"); // 4%
    
    // Max Salary Caps
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal bpjsKesehatanMaxSalary = new BigDecimal("12000000"); // 12M limit
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal bpjsJpMaxSalary = new BigDecimal("10042300"); // ~10M limit

    // Tax brackets stored as child collection
    @OneToMany(mappedBy = "settings", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("minAmount ASC")
    private List<TaxBracket> taxBrackets = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PayrollSettings() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getWorkingDaysPerMonth() { return workingDaysPerMonth; }
    public void setWorkingDaysPerMonth(int workingDaysPerMonth) { this.workingDaysPerMonth = workingDaysPerMonth; }

    public BigDecimal getOvertimeMultiplierFirstHour() { return overtimeMultiplierFirstHour; }
    public void setOvertimeMultiplierFirstHour(BigDecimal overtimeMultiplierFirstHour) { this.overtimeMultiplierFirstHour = overtimeMultiplierFirstHour; }

    public BigDecimal getOvertimeMultiplierSubsequentHours() { return overtimeMultiplierSubsequentHours; }
    public void setOvertimeMultiplierSubsequentHours(BigDecimal overtimeMultiplierSubsequentHours) { this.overtimeMultiplierSubsequentHours = overtimeMultiplierSubsequentHours; }

    public BigDecimal getBpjsJhtEmployeeRate() { return bpjsJhtEmployeeRate; }
    public void setBpjsJhtEmployeeRate(BigDecimal bpjsJhtEmployeeRate) { this.bpjsJhtEmployeeRate = bpjsJhtEmployeeRate; }

    public BigDecimal getBpjsJpEmployeeRate() { return bpjsJpEmployeeRate; }
    public void setBpjsJpEmployeeRate(BigDecimal bpjsJpEmployeeRate) { this.bpjsJpEmployeeRate = bpjsJpEmployeeRate; }

    public BigDecimal getBpjsJhtEmployerRate() { return bpjsJhtEmployerRate; }
    public void setBpjsJhtEmployerRate(BigDecimal bpjsJhtEmployerRate) { this.bpjsJhtEmployerRate = bpjsJhtEmployerRate; }

    public BigDecimal getBpjsJpEmployerRate() { return bpjsJpEmployerRate; }
    public void setBpjsJpEmployerRate(BigDecimal bpjsJpEmployerRate) { this.bpjsJpEmployerRate = bpjsJpEmployerRate; }

    public BigDecimal getBpjsJkkRate() { return bpjsJkkRate; }
    public void setBpjsJkkRate(BigDecimal bpjsJkkRate) { this.bpjsJkkRate = bpjsJkkRate; }

    public BigDecimal getBpjsJkmRate() { return bpjsJkmRate; }
    public void setBpjsJkmRate(BigDecimal bpjsJkmRate) { this.bpjsJkmRate = bpjsJkmRate; }

    public List<TaxBracket> getTaxBrackets() { return taxBrackets; }
    public void setTaxBrackets(List<TaxBracket> taxBrackets) { this.taxBrackets = taxBrackets; }

    public BigDecimal getBpjsKesehatanEmployeeRate() { return bpjsKesehatanEmployeeRate; }
    public void setBpjsKesehatanEmployeeRate(BigDecimal bpjsKesehatanEmployeeRate) { this.bpjsKesehatanEmployeeRate = bpjsKesehatanEmployeeRate; }

    public BigDecimal getBpjsKesehatanCompanyRate() { return bpjsKesehatanCompanyRate; }
    public void setBpjsKesehatanCompanyRate(BigDecimal bpjsKesehatanCompanyRate) { this.bpjsKesehatanCompanyRate = bpjsKesehatanCompanyRate; }

    public BigDecimal getBpjsKesehatanMaxSalary() { return bpjsKesehatanMaxSalary; }
    public void setBpjsKesehatanMaxSalary(BigDecimal bpjsKesehatanMaxSalary) { this.bpjsKesehatanMaxSalary = bpjsKesehatanMaxSalary; }

    public BigDecimal getBpjsJpMaxSalary() { return bpjsJpMaxSalary; }
    public void setBpjsJpMaxSalary(BigDecimal bpjsJpMaxSalary) { this.bpjsJpMaxSalary = bpjsJpMaxSalary; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
