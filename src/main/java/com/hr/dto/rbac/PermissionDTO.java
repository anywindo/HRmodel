package com.hr.dto.rbac;

public class PermissionDTO {
    private Long id;
    private String name;
    private String category;

    public PermissionDTO() {}

    public PermissionDTO(Long id, String name) {
        this.id = id;
        this.name = name;
        this.category = extractCategory(name);
    }

    private String extractCategory(String name) {
        if (name == null) return "Other";
        if (name.startsWith("employee:")) return "Employee Management";
        if (name.startsWith("department:")) return "Department Management";
        if (name.startsWith("position:")) return "Position Management";
        if (name.startsWith("company_profile:")) return "Company Profile";
        if (name.startsWith("leave:")) return "Leave Management";
        if (name.startsWith("payroll:")) return "Payroll Management";
        if (name.startsWith("attendance:")) return "Attendance Management";
        if (name.startsWith("role:") || name.startsWith("user:")) return "RBAC & Access Control";
        return "Other";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
