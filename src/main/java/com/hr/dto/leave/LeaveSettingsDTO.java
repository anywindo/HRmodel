package com.hr.dto.leave;

import java.math.BigDecimal;

public class LeaveSettingsDTO {
    private Long id;
    private boolean enableAccrual;
    private BigDecimal accrualRatePerMonth;
    private int maxCarryOverDays;
    private int defaultAnnualLeaveDays;
    private int defaultSickLeaveDays;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEnableAccrual() {
        return enableAccrual;
    }

    public void setEnableAccrual(boolean enableAccrual) {
        this.enableAccrual = enableAccrual;
    }

    public BigDecimal getAccrualRatePerMonth() {
        return accrualRatePerMonth;
    }

    public void setAccrualRatePerMonth(BigDecimal accrualRatePerMonth) {
        this.accrualRatePerMonth = accrualRatePerMonth;
    }

    public int getMaxCarryOverDays() {
        return maxCarryOverDays;
    }

    public void setMaxCarryOverDays(int maxCarryOverDays) {
        this.maxCarryOverDays = maxCarryOverDays;
    }

    public int getDefaultAnnualLeaveDays() {
        return defaultAnnualLeaveDays;
    }

    public void setDefaultAnnualLeaveDays(int defaultAnnualLeaveDays) {
        this.defaultAnnualLeaveDays = defaultAnnualLeaveDays;
    }

    public int getDefaultSickLeaveDays() {
        return defaultSickLeaveDays;
    }

    public void setDefaultSickLeaveDays(int defaultSickLeaveDays) {
        this.defaultSickLeaveDays = defaultSickLeaveDays;
    }
}
