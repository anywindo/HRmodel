package com.hr.dto.attendance;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OvertimeRequestDTO {
    private LocalDate date;
    private BigDecimal hoursRequested;
    private String reason;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getHoursRequested() { return hoursRequested; }
    public void setHoursRequested(BigDecimal hoursRequested) { this.hoursRequested = hoursRequested; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
