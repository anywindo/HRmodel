package com.hr.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeRequest {
    private String firstName;
    private String middleName;
    private String lastName;
    
    private String email;
    
    private String phoneCountryCode;
    private String phoneNumber;
    
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    
    private String gender;
    private String sex;
    
    private BigDecimal salaryAmount;
    private String salaryCurrency; // Unused in backend currently but accepted
    private String status;
    private String maritalStatus;
    private String positionId;

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneCountryCode() { return phoneCountryCode; }
    public void setPhoneCountryCode(String phoneCountryCode) { this.phoneCountryCode = phoneCountryCode; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public BigDecimal getSalaryAmount() { return salaryAmount; }
    public void setSalaryAmount(BigDecimal salaryAmount) { this.salaryAmount = salaryAmount; }
    public String getSalaryCurrency() { return salaryCurrency; }
    public void setSalaryCurrency(String salaryCurrency) { this.salaryCurrency = salaryCurrency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public String getPositionId() { return positionId; }
    public void setPositionId(String positionId) { this.positionId = positionId; }
}
