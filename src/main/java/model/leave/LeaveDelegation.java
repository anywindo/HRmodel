package model.leave;

import jakarta.persistence.*;
import model.employee.Employee;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave_delegations")
public class LeaveDelegation {

    @Id
    @Column(name = "delegation_id", updatable = false, nullable = false)
    private String delegationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegator_id", nullable = false)
    private Employee delegator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegatee_id", nullable = false)
    private Employee delegatee;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public LeaveDelegation() {}

    public LeaveDelegation(Employee delegator, Employee delegatee, LocalDate startDate, LocalDate endDate) {
        this.delegationId = UUID.randomUUID().toString();
        this.delegator = delegator;
        this.delegatee = delegatee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = true;
    }

    public String getDelegationId() { return delegationId; }
    public void setDelegationId(String delegationId) { this.delegationId = delegationId; }

    public Employee getDelegator() { return delegator; }
    public void setDelegator(Employee delegator) { this.delegator = delegator; }

    public Employee getDelegatee() { return delegatee; }
    public void setDelegatee(Employee delegatee) { this.delegatee = delegatee; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
