package com.hr.dto;

import model.department.Department;

public class DepartmentResponse {
    private String departmentId;
    private String name;
    private String description;

    public DepartmentResponse(Department department) {
        this.departmentId = department.getDepartmentId().getValue();
        this.name = department.getName();
        this.description = department.getDescription();
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
