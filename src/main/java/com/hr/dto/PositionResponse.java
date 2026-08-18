package com.hr.dto;

import model.position.Position;

public class PositionResponse {
    private String positionId;
    private String title;
    private String description;
    private DepartmentDto department;
    private String reportsToId;
    private String reportsToTitle;

    public PositionResponse(Position position) {
        this.positionId = position.getPositionId().getValue();
        this.title = position.getTitle();
        this.description = position.getDescription();
        
        if (position.getDepartment() != null) {
            this.department = new DepartmentDto(
                    position.getDepartment().getDepartmentId().getValue(),
                    position.getDepartment().getName()
            );
        }
        
        if (position.getReportsTo() != null) {
            this.reportsToId = position.getReportsTo().getPositionId().getValue();
            this.reportsToTitle = position.getReportsTo().getTitle();
        }
    }

    public String getPositionId() {
        return positionId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public DepartmentDto getDepartment() {
        return department;
    }

    public String getReportsToId() {
        return reportsToId;
    }
    
    public String getReportsToTitle() {
        return reportsToTitle;
    }

    public static class DepartmentDto {
        public String departmentId;
        public String name;
        public DepartmentDto(String id, String n) { this.departmentId = id; this.name = n; }
    }
}
