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
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
@SuppressWarnings("null")
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
        // 1. Create Departments
        Department execDept = new Department(new DepartmentId("D-EXEC"), "Executive", "Executive Leadership & Strategy", null);
        Department engDept = new Department(new DepartmentId("D-ENG"), "Engineering", "Software Architecture & Core Product Development", null);
        Department hrDept = new Department(new DepartmentId("D-HR"), "Human Resources", "People Operations, Talent Acquisition & Culture", null);
        Department mktDept = new Department(new DepartmentId("D-MKT"), "Marketing & Sales", "Brand Strategy, Growth & Customer Acquisition", null);
        Department finDept = new Department(new DepartmentId("D-FIN"), "Finance & Legal", "Financial Planning, Accounting & Legal Compliance", null);
        Department desDept = new Department(new DepartmentId("D-DES"), "Product & Design", "UI/UX Experience, User Research & Design Systems", null);

        departmentRepository.saveAll(List.of(execDept, engDept, hrDept, mktDept, finDept, desDept));

        // 2. Create Positions Structure
        // Executive
        Position ceo = new Position(new PositionId("P-CEO"), "Chief Executive Officer", "Top corporate executive leading company strategy", execDept, null);
        positionRepository.save(ceo);

        Position cto = new Position(new PositionId("P-CTO"), "Chief Technology Officer", "Head of Engineering and Technical Infrastructure", engDept, ceo);
        Position cmo = new Position(new PositionId("P-CMO"), "Chief Marketing Officer", "Head of Global Marketing and Brand Strategy", mktDept, ceo);
        Position cfo = new Position(new PositionId("P-CFO"), "Chief Financial Officer", "Head of Financial Operations and Legal Compliance", finDept, ceo);
        Position hrDir = new Position(new PositionId("P-HRDIR"), "VP of Human Resources", "Head of Global HR and Employee Experience", hrDept, ceo);
        Position headDesign = new Position(new PositionId("P-VP-DES"), "VP of Product Design", "Head of Product UX/UI Strategy", desDept, ceo);
        positionRepository.saveAll(List.of(cto, cmo, cfo, hrDir, headDesign));

        // Engineering
        Position principalArchitect = new Position(new PositionId("P-ARCH"), "Principal Software Architect", "Architecting scalable cloud systems", engDept, cto);
        Position engLead = new Position(new PositionId("P-ENG-LEAD"), "Engineering Lead", "Managing development teams and deliverables", engDept, cto);
        Position srDev = new Position(new PositionId("P-SDEV"), "Senior Software Engineer", "Senior full-stack development", engDept, engLead);
        Position jrDev = new Position(new PositionId("P-JDEV"), "Junior Software Engineer", "Frontend & Backend feature development", engDept, srDev);
        Position qaLead = new Position(new PositionId("P-QA-LEAD"), "QA Engineering Lead", "Test automation and quality assurance", engDept, engLead);
        positionRepository.saveAll(List.of(principalArchitect, engLead, srDev, jrDev, qaLead));

        // HR & People
        Position hrManager = new Position(new PositionId("P-HR-MGR"), "Talent Acquisition Manager", "Recruiting top tech talent", hrDept, hrDir);
        Position hrSpec = new Position(new PositionId("P-HRSPEC"), "People Operations Specialist", "Employee onboarding and benefits admin", hrDept, hrManager);
        positionRepository.saveAll(List.of(hrManager, hrSpec));

        // Marketing & Product
        Position mktLead = new Position(new PositionId("P-MKT-LEAD"), "Growth Marketing Lead", "Digital campaigns and user acquisition", mktDept, cmo);
        Position srDesigner = new Position(new PositionId("P-SR-DES"), "Senior Product Designer", "User experience and visual interface design", desDept, headDesign);
        Position finAnalyst = new Position(new PositionId("P-FIN-ANALYST"), "Senior Financial Analyst", "Budget forecasting and corporate reporting", finDept, cfo);
        positionRepository.saveAll(List.of(mktLead, srDesigner, finAnalyst));

        // 3. Update Department Heads
        execDept.updateDetails(execDept.getName(), execDept.getDescription(), ceo);
        engDept.updateDetails(engDept.getName(), engDept.getDescription(), cto);
        hrDept.updateDetails(hrDept.getName(), hrDept.getDescription(), hrDir);
        mktDept.updateDetails(mktDept.getName(), mktDept.getDescription(), cmo);
        finDept.updateDetails(finDept.getName(), finDept.getDescription(), cfo);
        desDept.updateDetails(desDept.getName(), desDept.getDescription(), headDesign);
        departmentRepository.saveAll(List.of(execDept, engDept, hrDept, mktDept, finDept, desDept));

        // 4. Create Diverse Employee Pool
        List<Employee> employees = new ArrayList<>();

        // C-Level & Leadership
        employees.add(createEmp("Alice", "", "Smith", "alice.smith@techcorp.com", "1234567890", "+1", 1979, 4, 12, 2012, 3, 1, Gender.WOMAN, Sex.FEMALE, "265000.00", MaritalStatus.MARRIED, ceo, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Robert", "James", "Chen", "robert.chen@techcorp.com", "9876543210", "+1", 1982, 8, 21, 2014, 6, 15, Gender.MAN, Sex.MALE, "210000.00", MaritalStatus.MARRIED, cto, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Sophia", "Elena", "Rodriguez", "sophia.rodriguez@techcorp.com", "5558881122", "+1", 1985, 11, 3, 2016, 1, 10, Gender.WOMAN, Sex.FEMALE, "195000.00", MaritalStatus.SINGLE, cmo, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Marcus", "Aurelius", "Vance", "marcus.vance@techcorp.com", "5557773344", "+1", 1980, 2, 14, 2015, 9, 1, Gender.MAN, Sex.MALE, "205000.00", MaritalStatus.DIVORCED, cfo, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Evelyn", "Grace", "Taylor", "evelyn.taylor@techcorp.com", "5554443333", "+1", 1983, 5, 25, 2013, 4, 1, Gender.WOMAN, Sex.FEMALE, "175000.00", MaritalStatus.MARRIED, hrDir, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Kiran", "N", "Patel", "kiran.patel@techcorp.com", "5552229988", "+1", 1987, 9, 17, 2018, 11, 5, Gender.NON_BINARY, Sex.UNSPECIFIED, "180000.00", MaritalStatus.SINGLE, headDesign, EmployeeStatus.ACTIVE));

        // Engineering & Product
        employees.add(createEmp("Tariq", "Hassan", "Al-Mansoor", "tariq.almansoor@techcorp.com", "5553332211", "+1", 1988, 1, 30, 2017, 7, 12, Gender.MAN, Sex.MALE, "165000.00", MaritalStatus.MARRIED, principalArchitect, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Elena", "Viktorovna", "Petrova", "elena.petrova@techcorp.com", "5556664422", "+1", 1991, 6, 18, 2019, 2, 20, Gender.WOMAN, Sex.FEMALE, "145000.00", MaritalStatus.SINGLE, engLead, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Charlie", "David", "Brown", "charlie.brown@techcorp.com", "5551234567", "+1", 1990, 7, 10, 2018, 9, 1, Gender.MAN, Sex.MALE, "130000.00", MaritalStatus.DIVORCED, srDev, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Aoi", "", "Takahashi", "aoi.takahashi@techcorp.com", "5559990011", "+1", 1994, 12, 5, 2021, 5, 10, Gender.GENDERFLUID, Sex.FEMALE, "125000.00", MaritalStatus.SINGLE, srDev, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Diana", "Marie", "Prince", "diana.prince@techcorp.com", "5559876543", "+1", 1998, 11, 5, 2022, 1, 15, Gender.WOMAN, Sex.FEMALE, "92000.00", MaritalStatus.SINGLE, jrDev, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Lucas", "Gabriel", "Silva", "lucas.silva@techcorp.com", "5554445566", "+1", 1999, 3, 22, 2023, 8, 1, Gender.MAN, Sex.MALE, "85000.00", MaritalStatus.SINGLE, jrDev, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Jordan", "Alex", "Taylor", "jordan.taylor@techcorp.com", "5558887766", "+1", 1993, 10, 14, 2020, 3, 15, Gender.AGENDER, Sex.UNSPECIFIED, "115000.00", MaritalStatus.PREFER_NOT_TO_SAY, qaLead, EmployeeStatus.ACTIVE));

        // HR, Marketing, Finance & Others
        employees.add(createEmp("Frank", "Thomas", "Castle", "frank.castle@techcorp.com", "5552221111", "+1", 1992, 8, 30, 2020, 11, 1, Gender.MAN, Sex.MALE, "88000.00", MaritalStatus.SINGLE, hrManager, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Mei-Ling", "", "Zhang", "meiling.zhang@techcorp.com", "5553334455", "+1", 1996, 4, 16, 2022, 9, 1, Gender.WOMAN, Sex.FEMALE, "76000.00", MaritalStatus.SINGLE, hrSpec, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Gabriel", "Mateo", "Fernandez", "gabriel.fernandez@techcorp.com", "5556667788", "+1", 1993, 7, 24, 2021, 2, 1, Gender.MAN, Sex.MALE, "105000.00", MaritalStatus.MARRIED, mktLead, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Chloe", "Isabelle", "Dubois", "chloe.dubois@techcorp.com", "5551112233", "+1", 1995, 9, 8, 2021, 10, 15, Gender.WOMAN, Sex.FEMALE, "118000.00", MaritalStatus.SINGLE, srDesigner, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Samuel", "Joseph", "Oak", "samuel.oak@techcorp.com", "5559993322", "+1", 1989, 12, 1, 2019, 4, 1, Gender.MAN, Sex.MALE, "128000.00", MaritalStatus.MARRIED, finAnalyst, EmployeeStatus.ACTIVE));

        // Inactive / Special Employment Statuses for Testing Dashboards & Filters
        employees.add(createEmp("Victor", "L", "Doom", "victor.doom@techcorp.com", "5556660099", "+1", 1984, 5, 10, 2016, 3, 1, Gender.MAN, Sex.MALE, "150000.00", MaritalStatus.DIVORCED, srDev, EmployeeStatus.TERMINATED));
        employees.add(createEmp("Samantha", "Jane", "Carter", "samantha.carter@techcorp.com", "5554448899", "+1", 1990, 8, 12, 2017, 6, 1, Gender.WOMAN, Sex.FEMALE, "135000.00", MaritalStatus.SEPARATED, srDev, EmployeeStatus.RESIGNED));
        employees.add(createEmp("Noah", "Ethan", "Williams", "noah.williams@techcorp.com", "5557778811", "+1", 1997, 2, 28, 2022, 11, 15, Gender.MAN, Sex.MALE, "80000.00", MaritalStatus.SINGLE, jrDev, EmployeeStatus.ON_LEAVE));
        employees.add(createEmp("Zoe", "Amara", "Kim", "zoe.kim@techcorp.com", "5552223344", "+1", 1996, 10, 9, 2023, 1, 10, Gender.OTHER, Sex.UNSPECIFIED, "79000.00", MaritalStatus.PREFER_NOT_TO_SAY, hrSpec, EmployeeStatus.SUSPENDED));

        employeeRepository.saveAll(employees);

        System.out.println("Enhanced diverse dummy data seeded successfully with " + employees.size() + " employees across 6 departments and 16 positions!");
    }

    private Employee createEmp(String firstName, String middleName, String lastName,
                               String email, String phoneNum, String countryCode,
                               int dobYear, int dobMonth, int dobDay,
                               int hireYear, int hireMonth, int hireDay,
                               Gender gender, Sex sex, String salaryStr,
                               MaritalStatus maritalStatus, Position position, EmployeeStatus status) {
        Employee emp = Employee.create(
                new FullName(firstName, middleName, lastName),
                new Email(email),
                new PhoneNumber(phoneNum, countryCode),
                LocalDate.of(dobYear, dobMonth, dobDay),
                LocalDate.of(hireYear, hireMonth, hireDay),
                gender,
                sex,
                new Salary(new BigDecimal(salaryStr)),
                maritalStatus,
                position
        );
        if (status == EmployeeStatus.TERMINATED) {
            emp.terminate();
        } else if (status == EmployeeStatus.RESIGNED) {
            emp.resign();
        } else if (status != EmployeeStatus.ACTIVE && status != null) {
            emp.updateDetails(null, null, null, null, status, null, null, null, null);
        }
        return emp;
    }
}
