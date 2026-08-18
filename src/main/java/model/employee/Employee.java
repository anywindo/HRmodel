package model.employee;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String employeeId;

    @Embedded
    private FullName fullName;

    @Embedded
    private Email email;

    @Embedded
    private PhoneNumber phoneNumber;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;

    @Embedded
    private Salary salary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaritalStatus maritalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private model.position.Position position;

    // JPA requires a no-arg constructor
    protected Employee() {}

    // Private constructor for Domain logic
    private Employee(String employeeId, FullName fullName, Email email, PhoneNumber phoneNumber,
                     LocalDate dateOfBirth, LocalDate hireDate, Gender gender, Sex sex, Salary salary, MaritalStatus maritalStatus, model.position.Position position) {
        
        validateAge(dateOfBirth, hireDate);
        
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.hireDate = hireDate;
        this.gender = gender;
        this.sex = sex;
        this.salary = salary;
        this.maritalStatus = maritalStatus != null ? maritalStatus : MaritalStatus.PREFER_NOT_TO_SAY;
        this.position = position;
        this.status = EmployeeStatus.ACTIVE;
    }

    public static Employee create(FullName fullName, Email email, PhoneNumber phoneNumber,
                                  LocalDate dateOfBirth, LocalDate hireDate, Gender gender, Sex sex, Salary salary, MaritalStatus maritalStatus, model.position.Position position) {
        String generatedEmployeeId = "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new Employee(generatedEmployeeId, fullName, email, phoneNumber, dateOfBirth, hireDate, gender, sex, salary, maritalStatus, position);
    }

    private void validateAge(LocalDate dateOfBirth, LocalDate hireDate) {
        if (dateOfBirth == null || hireDate == null) {
            throw new IllegalArgumentException("Date of birth and hire date are required");
        }
        int ageAtHire = Period.between(dateOfBirth, hireDate).getYears();
        if (ageAtHire < 18) {
            throw new IllegalArgumentException("Employee must be at least 18 years old at the time of hiring");
        }
    }

    // Domain Behaviors
    public void changeEmail(Email newEmail) {
        if (newEmail == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        this.email = newEmail;
    }

    public void updateSalary(Salary newSalary) {
        if (newSalary == null) {
            throw new IllegalArgumentException("Salary cannot be null");
        }
        this.salary = newSalary;
    }
    
    public void updateDetails(FullName newName, PhoneNumber newPhone, Gender newGender, Sex newSex, EmployeeStatus newStatus, LocalDate newDob, LocalDate newHireDate, MaritalStatus newMaritalStatus, model.position.Position newPosition) {
        if (newDob != null && newHireDate != null) {
            validateAge(newDob, newHireDate);
            this.dateOfBirth = newDob;
            this.hireDate = newHireDate;
        } else if (newDob != null) {
            validateAge(newDob, this.hireDate);
            this.dateOfBirth = newDob;
        } else if (newHireDate != null) {
            validateAge(this.dateOfBirth, newHireDate);
            this.hireDate = newHireDate;
        }

        if (newName != null) this.fullName = newName;
        if (newPhone != null) this.phoneNumber = newPhone;
        if (newGender != null) this.gender = newGender;
        if (newSex != null) this.sex = newSex;
        if (newStatus != null) this.status = newStatus;
        if (newMaritalStatus != null) this.maritalStatus = newMaritalStatus;
        if (newPosition != null) this.position = newPosition;
    }

    public void terminate() {
        if (this.status == EmployeeStatus.TERMINATED) {
            throw new IllegalStateException("Employee is already terminated");
        }
        this.status = EmployeeStatus.TERMINATED;
    }

    public void resign() {
        if (this.status == EmployeeStatus.RESIGNED) {
            throw new IllegalStateException("Employee has already resigned");
        }
        this.status = EmployeeStatus.RESIGNED;
    }

    // Getters
    public Long getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public FullName getFullName() { return fullName; }
    public Email getEmail() { return email; }
    public PhoneNumber getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public LocalDate getHireDate() { return hireDate; }
    public Gender getGender() { return gender; }
    public Sex getSex() { return sex; }
    public Salary getSalary() { return salary; }
    public EmployeeStatus getStatus() { return status; }
    public MaritalStatus getMaritalStatus() { return maritalStatus; }
    public model.position.Position getPosition() { return position; }
}
