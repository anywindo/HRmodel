package com.hr.dto;

import model.department.Department;

public class DepartmentResponse {
    private String departmentId;
    private String name;
    private String description;
    private PositionDto headPosition;

    public DepartmentResponse(Department department) {
        this.departmentId = department.getDepartmentId().getValue();
        this.name = department.getName();
        this.description = department.getDescription();
        if (department.getHeadOfDepartment() != null) {
            this.headPosition = new PositionDto(
                department.getHeadOfDepartment().getPositionId().getValue(),
                department.getHeadOfDepartment().getTitle()
            );
        }
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

    public PositionDto getHeadPosition() {
        return headPosition;
    }

    public static class PositionDto {
        public String positionId;
        public String title;
        public PositionDto(String id, String t) { this.positionId = id; this.title = t; }
    }
}
