package com.hr.config;

import model.department.Department;
import model.department.DepartmentId;
import model.employee.*;
import model.position.Position;
import model.position.PositionId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import repository.department.DepartmentRepository;
import repository.employee.EmployeeRepository;
import repository.position.PositionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;

    public DataSeeder(DepartmentRepository departmentRepository, PositionRepository positionRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (employeeRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        // 1. Create Departments (without heads initially since positions are not yet created)
        Department execDept = new Department(new DepartmentId("D-EXEC"), "Executive", "Executive Management", null);
        Department engDept = new Department(new DepartmentId("D-ENG"), "Engineering", "Software Engineering Department", null);
        Department hrDept = new Department(new DepartmentId("D-HR"), "Human Resources", "HR and People Operations", null);

        departmentRepository.saveAll(List.of(execDept, engDept, hrDept));

        // 2. Create Positions
        Position ceo = new Position(new PositionId("P-CEO"), "Chief Executive Officer", "Head of the company", execDept, null);
        positionRepository.save(ceo);

        Position cto = new Position(new PositionId("P-CTO"), "Chief Technology Officer", "Head of Engineering", engDept, ceo);
        Position hrDir = new Position(new PositionId("P-HRDIR"), "HR Director", "Head of HR", hrDept, ceo);
        positionRepository.saveAll(List.of(cto, hrDir));

        Position seniorDev = new Position(new PositionId("P-SDEV"), "Senior Developer", "Senior Software Engineer", engDept, cto);
        Position juniorDev = new Position(new PositionId("P-JDEV"), "Junior Developer", "Junior Software Engineer", engDept, seniorDev);
        Position hrSpecialist = new Position(new PositionId("P-HRSPEC"), "HR Specialist", "HR Generalist", hrDept, hrDir);
        
        positionRepository.saveAll(List.of(seniorDev, juniorDev, hrSpecialist));

        // 3. Update Departments with their heads
        execDept.updateDetails(execDept.getName(), execDept.getDescription(), ceo);
        engDept.updateDetails(engDept.getName(), engDept.getDescription(), cto);
        hrDept.updateDetails(hrDept.getName(), hrDept.getDescription(), hrDir);
        departmentRepository.saveAll(List.of(execDept, engDept, hrDept));

        // 4. Create Employees
        Employee empCeo = Employee.create(
                new FullName("Alice", "", "Smith"),
                new Email("alice.smith@company.com"),
                new PhoneNumber("1234567890", "+1"),
                LocalDate.of(1980, 1, 15),
                LocalDate.of(2010, 5, 1),
                Gender.WOMAN,
                Sex.FEMALE,
                new Salary(new BigDecimal("250000.00")),
                MaritalStatus.MARRIED,
                ceo
        );

        Employee empCto = Employee.create(
                new FullName("Bob", "James", "Johnson"),
                new Email("bob.johnson@company.com"),
                new PhoneNumber("9876543210", "+1"),
                LocalDate.of(1985, 3, 20),
                LocalDate.of(2015, 6, 10),
                Gender.MAN,
                Sex.MALE,
                new Salary(new BigDecimal("180000.00")),
                MaritalStatus.SINGLE,
                cto
        );

        Employee empSeniorDev = Employee.create(
                new FullName("Charlie", "", "Brown"),
                new Email("charlie.brown@company.com"),
                new PhoneNumber("5551234567", "+1"),
                LocalDate.of(1990, 7, 10),
                LocalDate.of(2018, 9, 1),
                Gender.MAN,
                Sex.MALE,
                new Salary(new BigDecimal("120000.00")),
                MaritalStatus.DIVORCED,
                seniorDev
        );

        Employee empJuniorDev = Employee.create(
                new FullName("Diana", "Marie", "Prince"),
                new Email("diana.prince@company.com"),
                new PhoneNumber("5559876543", "+1"),
                LocalDate.of(1998, 11, 5),
                LocalDate.of(2022, 1, 15),
                Gender.WOMAN,
                Sex.FEMALE,
                new Salary(new BigDecimal("80000.00")),
                MaritalStatus.SINGLE,
                juniorDev
        );

        Employee empHrDir = Employee.create(
                new FullName("Eve", "", "Adams"),
                new Email("eve.adams@company.com"),
                new PhoneNumber("5554443333", "+1"),
                LocalDate.of(1982, 5, 25),
                LocalDate.of(2012, 3, 1),
                Gender.WOMAN,
                Sex.FEMALE,
                new Salary(new BigDecimal("140000.00")),
                MaritalStatus.MARRIED,
                hrDir
        );

        Employee empHrSpec = Employee.create(
                new FullName("Frank", "T", "Castle"),
                new Email("frank.castle@company.com"),
                new PhoneNumber("5552221111", "+1"),
                LocalDate.of(1995, 8, 30),
                LocalDate.of(2020, 11, 1),
                Gender.MAN,
                Sex.MALE,
                new Salary(new BigDecimal("75000.00")),
                MaritalStatus.SINGLE,
                hrSpecialist
        );

        employeeRepository.saveAll(List.of(empCeo, empCto, empSeniorDev, empJuniorDev, empHrDir, empHrSpec));
        
        System.out.println("Dummy data seeded successfully.");
    }
}
