package model.leave;

import jakarta.persistence.*;
import model.employee.Employee;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @Column(name = "request_id", updatable = false, nullable = false)
    private String requestId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeaveStatus status;

    @ManyToOne
    @JoinColumn(name = "manager_approver_id")
    private Employee managerApprover;
    
    @ManyToOne
    @JoinColumn(name = "hr_approver_id")
    private Employee hrApprover;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public LeaveRequest(Employee employee, LeaveType leaveType, LocalDate startDate, LocalDate endDate, String reason) {
        this.requestId = UUID.randomUUID().toString();
        this.employee = employee;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        
        // Determine initial status based on whether the employee has a manager
        if (employee.getPosition() != null && employee.getPosition().getReportsTo() != null) {
            this.status = LeaveStatus.PENDING_MANAGER;
        } else {
            // If they don't have a manager, skip directly to HR approval
            this.status = LeaveStatus.PENDING_HR;
        }
        
        this.createdAt = LocalDateTime.now();
    }

    public LeaveRequest() {}

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LeaveStatus getStatus() { return status; }
    public void setStatus(LeaveStatus status) { this.status = status; }
    public Employee getManagerApprover() { return managerApprover; }
    public void setManagerApprover(Employee managerApprover) { this.managerApprover = managerApprover; }
    public Employee getHrApprover() { return hrApprover; }
    public void setHrApprover(Employee hrApprover) { this.hrApprover = hrApprover; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
