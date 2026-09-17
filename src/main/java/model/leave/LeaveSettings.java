package model.leave;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "leave_settings")
public class LeaveSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean enableAccrual = true;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal accrualRatePerMonth = new BigDecimal("1.25");

    @Column(nullable = false)
    private int maxCarryOverDays = 5;

    @Column(nullable = false)
    private int defaultAnnualLeaveDays = 15;

    @Column(nullable = false)
    private int defaultSickLeaveDays = 10;

    public LeaveSettings() {}

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
