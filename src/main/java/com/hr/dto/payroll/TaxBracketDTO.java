package com.hr.dto.payroll;

import java.math.BigDecimal;

public class TaxBracketDTO {
    private Long id;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal rate;

    public TaxBracketDTO() {}

    public TaxBracketDTO(Long id, BigDecimal minAmount, BigDecimal maxAmount, BigDecimal rate) {
        this.id = id;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.rate = rate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
}
