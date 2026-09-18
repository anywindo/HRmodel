package model.company;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public final class OfficeSettings implements Serializable {

    private String workStartTime;
    private String workEndTime;
    private String workingDays;
    private String breakStartTime;
    private String breakEndTime;
    private Integer lateGracePeriodMinutes;
    private String timezone;
    private String currency;
    private String taxIdNumber;
    private String websiteUrl;

    public OfficeSettings() {
        this.workStartTime = "09:00";
        this.workEndTime = "18:00";
        this.workingDays = "Monday - Friday";
        this.breakStartTime = "12:00";
        this.breakEndTime = "13:00";
        this.lateGracePeriodMinutes = 15;
        this.timezone = "Asia/Jakarta (GMT+7)";
        this.currency = "IDR";
        this.taxIdNumber = "";
        this.websiteUrl = "";
    }

    public OfficeSettings(
            String workStartTime,
            String workEndTime,
            String workingDays,
            String breakStartTime,
            String breakEndTime,
            Integer lateGracePeriodMinutes,
            String timezone,
            String currency,
            String taxIdNumber,
            String websiteUrl
    ) {
        this.workStartTime = (workStartTime != null && !workStartTime.isBlank()) ? workStartTime : "09:00";
        this.workEndTime = (workEndTime != null && !workEndTime.isBlank()) ? workEndTime : "18:00";
        this.workingDays = (workingDays != null && !workingDays.isBlank()) ? workingDays : "Monday - Friday";
        this.breakStartTime = (breakStartTime != null && !breakStartTime.isBlank()) ? breakStartTime : "12:00";
        this.breakEndTime = (breakEndTime != null && !breakEndTime.isBlank()) ? breakEndTime : "13:00";
        this.lateGracePeriodMinutes = lateGracePeriodMinutes != null ? lateGracePeriodMinutes : 15;
        this.timezone = (timezone != null && !timezone.isBlank()) ? timezone : "Asia/Jakarta (GMT+7)";
        this.currency = (currency != null && !currency.isBlank()) ? currency : "IDR";
        this.taxIdNumber = taxIdNumber != null ? taxIdNumber : "";
        this.websiteUrl = websiteUrl != null ? websiteUrl : "";
    }

    public static OfficeSettings defaultSettings() {
        return new OfficeSettings();
    }

    public String getWorkStartTime() {
        return workStartTime;
    }

    public String getWorkEndTime() {
        return workEndTime;
    }

    public String getWorkingDays() {
        return workingDays;
    }

    public String getBreakStartTime() {
        return breakStartTime;
    }

    public String getBreakEndTime() {
        return breakEndTime;
    }

    public Integer getLateGracePeriodMinutes() {
        return lateGracePeriodMinutes;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTaxIdNumber() {
        return taxIdNumber;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfficeSettings that)) return false;
        return Objects.equals(workStartTime, that.workStartTime) &&
                Objects.equals(workEndTime, that.workEndTime) &&
                Objects.equals(workingDays, that.workingDays) &&
                Objects.equals(breakStartTime, that.breakStartTime) &&
                Objects.equals(breakEndTime, that.breakEndTime) &&
                Objects.equals(lateGracePeriodMinutes, that.lateGracePeriodMinutes) &&
                Objects.equals(timezone, that.timezone) &&
                Objects.equals(currency, that.currency) &&
                Objects.equals(taxIdNumber, that.taxIdNumber) &&
                Objects.equals(websiteUrl, that.websiteUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                workStartTime, workEndTime, workingDays,
                breakStartTime, breakEndTime, lateGracePeriodMinutes,
                timezone, currency, taxIdNumber, websiteUrl
        );
    }
}
