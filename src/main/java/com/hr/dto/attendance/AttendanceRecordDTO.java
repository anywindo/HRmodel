package com.hr.dto.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import model.attendance.AttendanceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRecordDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime checkInTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime breakStartTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime breakEndTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime checkOutTime;

    private AttendanceStatus status;
    private BigDecimal overtimeHours;
    private String notes;

    public AttendanceRecordDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalTime getBreakStartTime() { return breakStartTime; }
    public void setBreakStartTime(LocalTime breakStartTime) { this.breakStartTime = breakStartTime; }

    public LocalTime getBreakEndTime() { return breakEndTime; }
    public void setBreakEndTime(LocalTime breakEndTime) { this.breakEndTime = breakEndTime; }

    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

