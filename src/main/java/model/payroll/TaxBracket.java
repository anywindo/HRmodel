package model.payroll;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tax_brackets")
public class TaxBracket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Annual threshold floor (e.g. 0 for first bracket)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal minAmount;

    // Annual threshold ceiling (null for top bracket = unlimited)
    @Column(precision = 15, scale = 2)
    private BigDecimal maxAmount;

    // Tax rate as decimal (e.g. 0.05 for 5%)
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal rate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settings_id", nullable = false)
    private PayrollSettings settings;

    public TaxBracket() {}

    public TaxBracket(BigDecimal minAmount, BigDecimal maxAmount, BigDecimal rate, PayrollSettings settings) {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.rate = rate;
        this.settings = settings;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public PayrollSettings getSettings() { return settings; }
    public void setSettings(PayrollSettings settings) { this.settings = settings; }
}
