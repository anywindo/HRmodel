package com.hr.dto;

public class DepartmentRequest {
    private String departmentId;
    private String name;
    private String description;
    private String headPositionId;

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHeadPositionId() {
        return headPositionId;
    }

    public void setHeadPositionId(String headPositionId) {
        this.headPositionId = headPositionId;
    }
}
