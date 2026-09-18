package com.hr.dto.payroll;

import java.math.BigDecimal;
import java.util.List;

public class PayrollSettingsDTO {
    private Long id;
    private int workingDaysPerMonth;
    private BigDecimal overtimeMultiplierFirstHour;
    private BigDecimal overtimeMultiplierSubsequentHours;
    private BigDecimal bpjsJhtEmployeeRate;
    private BigDecimal bpjsJpEmployeeRate;
    private BigDecimal bpjsJhtEmployerRate;
    private BigDecimal bpjsJpEmployerRate;
    private BigDecimal bpjsJkkRate;
    private BigDecimal bpjsJkmRate;
    private List<TaxBracketDTO> taxBrackets;

    public PayrollSettingsDTO() {}

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

    public List<TaxBracketDTO> getTaxBrackets() { return taxBrackets; }
    public void setTaxBrackets(List<TaxBracketDTO> taxBrackets) { this.taxBrackets = taxBrackets; }
}
