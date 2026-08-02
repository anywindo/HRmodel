package com.hr.dto;

import model.employee.Employee;

import java.math.BigDecimal;

public class EmployeeResponse {
    private Long id;
    private String employeeId;
    
    private FullNameDto fullName;
    private EmailDto email;
    private PhoneNumberDto phoneNumber;
    
    private String dateOfBirth;
    private String hireDate;
    
    private String gender;
    private String sex;
    private SalaryDto salary;
    private String status;
    private String maritalStatus;
    private DepartmentDto department;

    public EmployeeResponse(Employee employee) {
        this.id = employee.getId();
        this.employeeId = employee.getEmployeeId();
        this.fullName = new FullNameDto(
            employee.getFullName().getFirstName(),
            employee.getFullName().getMiddleName(),
            employee.getFullName().getLastName()
        );
        this.email = new EmailDto(employee.getEmail().getValue());
        this.phoneNumber = new PhoneNumberDto(
            employee.getPhoneNumber().getCountryCode(),
            employee.getPhoneNumber().getPhoneNumber()
        );
        this.dateOfBirth = employee.getDateOfBirth().toString();
        this.hireDate = employee.getHireDate().toString();
        this.gender = employee.getGender().name();
        this.sex = employee.getSex().name();
        this.salary = new SalaryDto(
            employee.getSalary().getAmount(),
            "USD"
        );
        this.status = employee.getStatus().name();
        this.maritalStatus = employee.getMaritalStatus() != null ? employee.getMaritalStatus().name() : null;
        if (employee.getDepartment() != null) {
            this.department = new DepartmentDto(
                employee.getDepartment().getDepartmentId().getValue(),
                employee.getDepartment().getName()
            );
        }
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

    public static class DepartmentDto {
        public String departmentId;
        public String name;
        public DepartmentDto(String id, String n) { this.departmentId = id; this.name = n; }
    }

    // Getters
    public Long getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public FullNameDto getFullName() { return fullName; }
    public EmailDto getEmail() { return email; }
    public PhoneNumberDto getPhoneNumber() { return phoneNumber; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getHireDate() { return hireDate; }
    public String getGender() { return gender; }
    public String getSex() { return sex; }
    public SalaryDto getSalary() { return salary; }
    public String getStatus() { return status; }
    public String getMaritalStatus() { return maritalStatus; }
    public DepartmentDto getDepartment() { return department; }
}
