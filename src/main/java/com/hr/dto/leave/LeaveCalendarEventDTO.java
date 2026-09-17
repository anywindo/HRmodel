package com.hr.dto.leave;

import java.time.LocalDate;

public class LeaveCalendarEventDTO {
    private String id;
    private String title;
    private String eventType; // "LEAVE" or "HOLIDAY"
    private String leaveType; // ANNUAL, SICK, etc. (null for holidays)
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String employeeId;
    private String departmentName;

    public LeaveCalendarEventDTO() {}

    // Factory for leave requests
    public static LeaveCalendarEventDTO fromLeaveRequest(
            String requestId, String employeeName, String leaveType,
            LocalDate startDate, LocalDate endDate, String status,
            String employeeId, String departmentName) {
        LeaveCalendarEventDTO dto = new LeaveCalendarEventDTO();
        dto.id = requestId;
        dto.title = employeeName;
        dto.eventType = "LEAVE";
        dto.leaveType = leaveType;
        dto.startDate = startDate;
        dto.endDate = endDate;
        dto.status = status;
        dto.employeeId = employeeId;
        dto.departmentName = departmentName;
        return dto;
    }

    // Factory for public holidays
    public static LeaveCalendarEventDTO fromHoliday(Long holidayId, String name, LocalDate date) {
        LeaveCalendarEventDTO dto = new LeaveCalendarEventDTO();
        dto.id = "holiday-" + holidayId;
        dto.title = name;
        dto.eventType = "HOLIDAY";
        dto.startDate = date;
        dto.endDate = date;
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}
