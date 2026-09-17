package com.hr.dto.leave;

import model.leave.LeaveDelegation;
import java.time.LocalDate;

public class LeaveDelegationDTO {
    private String delegationId;
    private String delegatorId;
    private String delegatorName;
    private String delegateeId;
    private String delegateeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    public LeaveDelegationDTO() {}

    public LeaveDelegationDTO(LeaveDelegation delegation) {
        this.delegationId = delegation.getDelegationId();
        this.delegatorId = delegation.getDelegator().getEmployeeId();
        this.delegatorName = delegation.getDelegator().getFullName().getFirstName() + " " + delegation.getDelegator().getFullName().getLastName();
        this.delegateeId = delegation.getDelegatee().getEmployeeId();
        this.delegateeName = delegation.getDelegatee().getFullName().getFirstName() + " " + delegation.getDelegatee().getFullName().getLastName();
        this.startDate = delegation.getStartDate();
        this.endDate = delegation.getEndDate();
        this.active = delegation.isActive();
    }

    public String getDelegationId() { return delegationId; }
    public void setDelegationId(String delegationId) { this.delegationId = delegationId; }
    
    public String getDelegatorId() { return delegatorId; }
    public void setDelegatorId(String delegatorId) { this.delegatorId = delegatorId; }
    
    public String getDelegatorName() { return delegatorName; }
    public void setDelegatorName(String delegatorName) { this.delegatorName = delegatorName; }
    
    public String getDelegateeId() { return delegateeId; }
    public void setDelegateeId(String delegateeId) { this.delegateeId = delegateeId; }
    
    public String getDelegateeName() { return delegateeName; }
    public void setDelegateeName(String delegateeName) { this.delegateeName = delegateeName; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
