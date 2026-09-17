package com.hr.dto.rbac;

import java.util.List;

public class UserResponseDTO {
    private Long id;
    private String email;
    private boolean isActive;
    private String employeeId;
    private String employeeName;
    private String departmentName;
    private String positionTitle;
    private List<RoleSummaryDTO> roles;

    public UserResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getPositionTitle() { return positionTitle; }
    public void setPositionTitle(String positionTitle) { this.positionTitle = positionTitle; }
    public List<RoleSummaryDTO> getRoles() { return roles; }
    public void setRoles(List<RoleSummaryDTO> roles) { this.roles = roles; }
}
