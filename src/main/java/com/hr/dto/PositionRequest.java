package com.hr.dto;

public class PositionRequest {
    private String positionId;
    private String title;
    private String description;
    private String departmentId;
    private String reportsToId;

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getReportsToId() {
        return reportsToId;
    }

    public void setReportsToId(String reportsToId) {
        this.reportsToId = reportsToId;
    }
}
