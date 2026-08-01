package com.hr.dto;

import model.employee.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeResponse {
    private String employeeId;
    
    private FullNameDto fullName;
    private EmailDto email;
    private PhoneNumberDto phoneNumber;
    
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    
    private String gender;
    private String sex;
    private SalaryDto salary;
    private String status;

    public EmployeeResponse(Employee emp) {
        this.employeeId = emp.getEmployeeId();
        this.fullName = new FullNameDto(emp.getFullName().getFirstName(), emp.getFullName().getMiddleName(), emp.getFullName().getLastName());
        this.email = new EmailDto(emp.getEmail().getValue());
        this.phoneNumber = new PhoneNumberDto(emp.getPhoneNumber().getCountryCode(), emp.getPhoneNumber().getPhoneNumber());
        this.dateOfBirth = emp.getDateOfBirth();
        this.hireDate = emp.getHireDate();
        this.gender = emp.getGender().name();
        this.sex = emp.getSex().name();
        this.salary = new SalaryDto(emp.getSalary().getAmount(), "USD"); // default currency
        this.status = emp.getStatus().name();
    }

    public static class FullNameDto {
        public String firstName;
        public String middleName;
        public String lastName;
        public FullNameDto(String f, String m, String l) { this.firstName = f; this.middleName = m; this.lastName = l; }
    }

    public static class EmailDto {
        public String address;
        public EmailDto(String a) { this.address = a; }
    }

    public static class PhoneNumberDto {
        public String countryCode;
        public String number;
        public PhoneNumberDto(String c, String n) { this.countryCode = c; this.number = n; }
    }

    public static class SalaryDto {
        public BigDecimal amount;
        public String currency;
        public SalaryDto(BigDecimal a, String c) { this.amount = a; this.currency = c; }
    }

    // Getters
    public String getEmployeeId() { return employeeId; }
    public FullNameDto getFullName() { return fullName; }
    public EmailDto getEmail() { return email; }
    public PhoneNumberDto getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public LocalDate getHireDate() { return hireDate; }
    public String getGender() { return gender; }
    public String getSex() { return sex; }
    public SalaryDto getSalary() { return salary; }
    public String getStatus() { return status; }
}
