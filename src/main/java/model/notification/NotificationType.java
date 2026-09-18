package model.notification;

public enum NotificationType {
    // Leave & Time Off
    LEAVE_SUBMITTED,
    LEAVE_APPROVED_MANAGER,
    LEAVE_APPROVED_HR,
    LEAVE_REJECTED,
    LEAVE_CANCELLED,
    DELEGATION_CREATED,
    DELEGATION_REVOKED,

    // Overtime
    OVERTIME_REQUEST,
    OVERTIME_APPROVED,
    OVERTIME_REJECTED,

    // Employee & Profile
    EMPLOYEE_ONBOARDED,
    EMPLOYEE_PROFILE_UPDATED,
    EMPLOYEE_STATUS_CHANGED,

    // Organization & Hierarchy
    DEPARTMENT_ASSIGNED,
    POSITION_ASSIGNED,

    // Security & RBAC
    ROLE_ASSIGNED,
    ROLE_REVOKED,
    ACCOUNT_ACTIVATED,

    // General & System
    SYSTEM_ANNOUNCEMENT,
    GENERAL
}

