package com.hr.dto.leave;

import java.time.LocalDate;

public class PublicHolidayDTO {
    private Long holidayId;
    private LocalDate date;
    private String description;

    public PublicHolidayDTO() {}

    public PublicHolidayDTO(Long holidayId, LocalDate date, String description) {
        this.holidayId = holidayId;
        this.date = date;
        this.description = description;
    }

    public Long getHolidayId() {
        return holidayId;
    }

    public void setHolidayId(Long holidayId) {
        this.holidayId = holidayId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
