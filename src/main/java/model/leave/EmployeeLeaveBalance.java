package model.leave;

import jakarta.persistence.*;
import model.employee.Employee;

import java.util.UUID;

@Entity
@Table(name = "employee_leave_balances")
public class EmployeeLeaveBalance {

    @Id
    @Column(name = "balance_id", updatable = false, nullable = false)
    private String balanceId;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "annual_leave_days", nullable = false)
    private int annualLeaveDays;

    @Column(name = "sick_leave_days", nullable = false)
    private int sickLeaveDays;

    public EmployeeLeaveBalance() {
    }

    public EmployeeLeaveBalance(Employee employee, int annualLeaveDays, int sickLeaveDays) {
        this.balanceId = UUID.randomUUID().toString();
        this.employee = employee;
        this.annualLeaveDays = annualLeaveDays;
        this.sickLeaveDays = sickLeaveDays;
    }

    public String getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(String balanceId) {
        this.balanceId = balanceId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public int getAnnualLeaveDays() {
        return annualLeaveDays;
    }

    public void setAnnualLeaveDays(int annualLeaveDays) {
        this.annualLeaveDays = annualLeaveDays;
    }

    public int getSickLeaveDays() {
        return sickLeaveDays;
    }

    public void setSickLeaveDays(int sickLeaveDays) {
        this.sickLeaveDays = sickLeaveDays;
    }
}
