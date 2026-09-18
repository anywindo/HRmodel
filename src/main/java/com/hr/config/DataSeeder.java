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
import model.company.*;
import repository.company.CompanyProfileRepository;
import repository.department.DepartmentRepository;
import repository.employee.EmployeeRepository;
import repository.position.PositionRepository;
import model.auth.Role;
import model.auth.Permission;
import repository.auth.RoleRepository;
import repository.auth.PermissionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import model.leave.EmployeeLeaveBalance;
import model.leave.LeaveRequest;
import model.leave.LeaveType;
import model.leave.LeaveStatus;
import model.leave.LeaveSettings;
import model.leave.PublicHoliday;
import model.leave.LeaveDelegation;
import repository.leave.EmployeeLeaveBalanceRepository;
import repository.leave.LeaveRequestRepository;
import repository.leave.LeaveDelegationRepository;
import repository.leave.LeaveSettingsRepository;
import repository.leave.PublicHolidayRepository;
import repository.attendance.AttendanceRecordRepository;
import repository.attendance.OvertimeRequestRepository;
import repository.payroll.PayrollSettingsRepository;
import repository.payroll.PayrollRunRepository;
import repository.payroll.PayrollItemRepository;
import repository.notification.NotificationRepository;
import service.payroll.PayrollCalculationService;
import model.attendance.AttendanceRecord;
import model.attendance.AttendanceStatus;
import model.attendance.OvertimeRequest;
import model.attendance.OvertimeRequestStatus;
import model.payroll.PayrollSettings;
import model.payroll.TaxBracket;
import model.payroll.PayrollRun;
import model.payroll.PayrollItem;
import model.payroll.PayrollStatus;
import model.notification.Notification;
import model.notification.NotificationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
@SuppressWarnings("null")
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeLeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final repository.auth.SystemAdminRepository systemAdminRepository;
    private final LeaveDelegationRepository leaveDelegationRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final OvertimeRequestRepository overtimeRequestRepository;
    private final PayrollSettingsRepository payrollSettingsRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final PayrollCalculationService payrollCalculationService;
    private final NotificationRepository notificationRepository;
    private final LeaveSettingsRepository leaveSettingsRepository;
    private final PublicHolidayRepository publicHolidayRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public DataSeeder(DepartmentRepository departmentRepository,
                      PositionRepository positionRepository,
                      EmployeeRepository employeeRepository,
                      CompanyProfileRepository companyProfileRepository,
                      RoleRepository roleRepository,
                      PermissionRepository permissionRepository,
                      PasswordEncoder passwordEncoder,
                      EmployeeLeaveBalanceRepository leaveBalanceRepository,
                      LeaveRequestRepository leaveRequestRepository,
                      repository.auth.SystemAdminRepository systemAdminRepository,
                      LeaveDelegationRepository leaveDelegationRepository,
                      AttendanceRecordRepository attendanceRecordRepository,
                      OvertimeRequestRepository overtimeRequestRepository,
                      PayrollSettingsRepository payrollSettingsRepository,
                      PayrollRunRepository payrollRunRepository,
                      PayrollItemRepository payrollItemRepository,
                      PayrollCalculationService payrollCalculationService,
                      NotificationRepository notificationRepository,
                      LeaveSettingsRepository leaveSettingsRepository,
                      PublicHolidayRepository publicHolidayRepository,
                      org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.employeeRepository = employeeRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.systemAdminRepository = systemAdminRepository;
        this.leaveDelegationRepository = leaveDelegationRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.overtimeRequestRepository = overtimeRequestRepository;
        this.payrollSettingsRepository = payrollSettingsRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.payrollItemRepository = payrollItemRepository;
        this.payrollCalculationService = payrollCalculationService;
        this.notificationRepository = notificationRepository;
        this.leaveSettingsRepository = leaveSettingsRepository;
        this.publicHolidayRepository = publicHolidayRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        fixDatabaseColumnTypes();
        if (companyProfileRepository.count() == 0) {
            seedCompanyProfile();
        }
        if (payrollSettingsRepository.count() == 0) {
            seedPayrollSettings();
        }
        if (leaveSettingsRepository.count() == 0) {
            seedLeaveSettings();
        }
        if (publicHolidayRepository.count() < 15) {
            publicHolidayRepository.deleteAll();
            seedPublicHolidays();
        }
        if (employeeRepository.count() < 25) {
            if (employeeRepository.count() > 0) {
                clearPartialData();
            }
            seedData();
        }
        seedAuthData();
    }

    private void fixDatabaseColumnTypes() {
        try {
            jdbcTemplate.execute("ALTER TABLE payroll_runs MODIFY COLUMN status VARCHAR(32) NOT NULL");
        } catch (Exception e) {
            System.out.println("Note: alter payroll_runs status: " + e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE leave_requests MODIFY COLUMN status VARCHAR(32) NOT NULL");
        } catch (Exception e) {
            System.out.println("Note: alter leave_requests status: " + e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE leave_requests MODIFY COLUMN leave_type VARCHAR(32) NOT NULL");
        } catch (Exception e) {
            System.out.println("Note: alter leave_requests leave_type: " + e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE overtime_requests MODIFY COLUMN status VARCHAR(32) NOT NULL");
        } catch (Exception e) {
            System.out.println("Note: alter overtime_requests status: " + e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE attendance_records MODIFY COLUMN status VARCHAR(32) NOT NULL");
        } catch (Exception e) {
            System.out.println("Note: alter attendance_records status: " + e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE notifications MODIFY COLUMN type VARCHAR(64) NOT NULL");
        } catch (Exception e) {
            System.out.println("Note: alter notifications type: " + e.getMessage());
        }
    }

    private void clearPartialData() {
        notificationRepository.deleteAllInBatch();
        overtimeRequestRepository.deleteAllInBatch();
        attendanceRecordRepository.deleteAllInBatch();
        payrollItemRepository.deleteAllInBatch();
        payrollRunRepository.deleteAllInBatch();
        leaveRequestRepository.deleteAllInBatch();
        leaveDelegationRepository.deleteAllInBatch();
        leaveBalanceRepository.deleteAllInBatch();

        // Unlink department heads to break circular FK references with position table
        List<Department> depts = departmentRepository.findAll();
        for (Department d : depts) {
            d.updateDetails(d.getName(), d.getDescription(), null);
        }
        departmentRepository.saveAllAndFlush(depts);

        employeeRepository.deleteAllInBatch();

        // Unlink position reporting hierarchy to break self-referential FK constraints
        List<Position> posList = positionRepository.findAll();
        for (Position p : posList) {
            p.updateDetails(p.getTitle(), p.getDescription(), p.getDepartment(), null);
        }
        positionRepository.saveAllAndFlush(posList);

        positionRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        System.out.println("Cleared legacy partial database records to allow complete seeder execution!");
    }

    private void seedCompanyProfile() {
        Address address = new Address(
                "100 Innovation Way, Suite 500",
                "San Francisco",
                "CA",
                "94105",
                "USA"
        );
        CompanyDetail detail = new CompanyDetail(
                "https://via.placeholder.com/150?text=TechCorp+Logo",
                "TechCorp Global Inc.",
                address
        );
        CompanyContact contact = new CompanyContact(
                new PhoneNumber("5550199", "+1"),
                new PhoneNumber("5550198", "+1"),
                new Email("contact@techcorp.com")
        );
        CompanyProfile profile = CompanyProfile.create(detail, contact);
        companyProfileRepository.save(profile);
        System.out.println("Company profile seeded successfully!");
    }

    private void seedPayrollSettings() {
        PayrollSettings settings = new PayrollSettings();
        settings.setWorkingDaysPerMonth(22);
        settings.setOvertimeMultiplierFirstHour(new BigDecimal("1.5"));
        settings.setOvertimeMultiplierSubsequentHours(new BigDecimal("2.0"));
        settings.setBpjsJhtEmployeeRate(new BigDecimal("0.02"));
        settings.setBpjsJpEmployeeRate(new BigDecimal("0.01"));
        settings.setBpjsJhtEmployerRate(new BigDecimal("0.037"));
        settings.setBpjsJpEmployerRate(new BigDecimal("0.02"));
        settings.setBpjsJkkRate(new BigDecimal("0.0024"));
        settings.setBpjsJkmRate(new BigDecimal("0.003"));

        List<TaxBracket> brackets = new ArrayList<>();
        brackets.add(new TaxBracket(new BigDecimal("0"), new BigDecimal("60000000"), new BigDecimal("0.05"), settings));
        brackets.add(new TaxBracket(new BigDecimal("60000000"), new BigDecimal("250000000"), new BigDecimal("0.15"), settings));
        brackets.add(new TaxBracket(new BigDecimal("250000000"), new BigDecimal("500000000"), new BigDecimal("0.25"), settings));
        brackets.add(new TaxBracket(new BigDecimal("500000000"), new BigDecimal("5000000000"), new BigDecimal("0.30"), settings));
        brackets.add(new TaxBracket(new BigDecimal("5000000000"), null, new BigDecimal("0.35"), settings));

        settings.setTaxBrackets(brackets);
        payrollSettingsRepository.save(settings);
        System.out.println("Default payroll settings seeded!");
    }

    private void seedLeaveSettings() {
        LeaveSettings settings = new LeaveSettings();
        settings.setEnableAccrual(true);
        settings.setAccrualRatePerMonth(new BigDecimal("1.25"));
        settings.setMaxCarryOverDays(5);
        settings.setDefaultAnnualLeaveDays(12);
        settings.setDefaultSickLeaveDays(10);
        leaveSettingsRepository.save(settings);
        System.out.println("Default leave settings seeded!");
    }

    private void seedPublicHolidays() {
        List<PublicHoliday> holidays = List.of(
            // 2025 Holidays
            new PublicHoliday(LocalDate.of(2025, 1, 1), "New Year's Day 2025"),
            new PublicHoliday(LocalDate.of(2025, 1, 20), "MLK Jr. Day 2025"),
            new PublicHoliday(LocalDate.of(2025, 5, 26), "Memorial Day 2025"),
            new PublicHoliday(LocalDate.of(2025, 7, 4), "Independence Day 2025"),
            new PublicHoliday(LocalDate.of(2025, 9, 1), "Labor Day 2025"),
            new PublicHoliday(LocalDate.of(2025, 11, 27), "Thanksgiving Day 2025"),
            new PublicHoliday(LocalDate.of(2025, 12, 25), "Christmas Day 2025"),
            // 2026 Holidays
            new PublicHoliday(LocalDate.of(2026, 1, 1), "New Year's Day 2026"),
            new PublicHoliday(LocalDate.of(2026, 1, 19), "Martin Luther King Jr. Day"),
            new PublicHoliday(LocalDate.of(2026, 2, 16), "Presidents' Day"),
            new PublicHoliday(LocalDate.of(2026, 5, 25), "Memorial Day"),
            new PublicHoliday(LocalDate.of(2026, 6, 19), "Juneteenth National Independence Day"),
            new PublicHoliday(LocalDate.of(2026, 7, 4), "Independence Day"),
            new PublicHoliday(LocalDate.of(2026, 9, 7), "Labor Day"),
            new PublicHoliday(LocalDate.of(2026, 10, 12), "Columbus Day"),
            new PublicHoliday(LocalDate.of(2026, 11, 11), "Veterans Day"),
            new PublicHoliday(LocalDate.of(2026, 11, 26), "Thanksgiving Day"),
            new PublicHoliday(LocalDate.of(2026, 12, 25), "Christmas Day")
        );
        publicHolidayRepository.saveAll(holidays);
        System.out.println(holidays.size() + " Public holidays seeded successfully!");
    }

    private void seedData() {
        // 1. Create 15 Departments
        Department execDept = new Department(new DepartmentId("D-EXEC"), "Executive Leadership", "Executive Strategy & Global Operations", null);
        Department engDept = new Department(new DepartmentId("D-ENG"), "Engineering & Cloud", "Software Architecture, Infrastructure & DevOps", null);
        Department hrDept = new Department(new DepartmentId("D-HR"), "Human Resources", "People Operations, Talent Acquisition & Employee Relations", null);
        Department mktDept = new Department(new DepartmentId("D-MKT"), "Growth & Marketing", "Brand Strategy, Digital Campaigns & User Acquisition", null);
        Department finDept = new Department(new DepartmentId("D-FIN"), "Finance & Legal", "Corporate Accounting, Tax Compliance & Legal Affairs", null);
        Department desDept = new Department(new DepartmentId("D-DES"), "Product & Design", "UI/UX Experience, Product Discovery & Visual Systems", null);
        Department csDept = new Department(new DepartmentId("D-CS"), "Customer Operations", "Customer Success, Support & Technical Services", null);
        Department secDept = new Department(new DepartmentId("D-SEC"), "Information Security & Compliance", "Cybersecurity, SOC Monitoring & Regulatory Compliance", null);
        Department dataDept = new Department(new DepartmentId("D-DATA"), "Data Science & AI", "Machine Learning, Analytics & Data Engineering", null);
        Department salesDept = new Department(new DepartmentId("D-SALES"), "Enterprise Sales", "B2B Sales, Key Accounts & Revenue Operations", null);
        Department scmDept = new Department(new DepartmentId("D-SCM"), "Supply Chain & Logistics", "Global Logistics, Procurement & Inventory Management", null);
        Department rndDept = new Department(new DepartmentId("D-RND"), "Research & Innovation", "Advanced Research, Patents & Future Tech Strategy", null);
        Department qaDept = new Department(new DepartmentId("D-QA"), "Quality Assurance", "Automated Testing, Reliability & Release Engineering", null);
        Department facDept = new Department(new DepartmentId("D-FAC"), "Workplace & Facilities", "Real Estate, Office Safety & Environmental Health", null);
        Department legalDept = new Department(new DepartmentId("D-LEGAL"), "Legal & IP Affairs", "Corporate Governance, Contracts & Patent Law", null);

        departmentRepository.saveAll(List.of(
            execDept, engDept, hrDept, mktDept, finDept, desDept, csDept, secDept,
            dataDept, salesDept, scmDept, rndDept, qaDept, facDept, legalDept
        ));

        // 2. Create 25 Positions Structure
        Position ceo = new Position(new PositionId("P-CEO"), "Chief Executive Officer", "Leading company strategy and vision", execDept, null);
        positionRepository.save(ceo);

        Position cto = new Position(new PositionId("P-CTO"), "Chief Technology Officer", "Technical infrastructure and engineering strategy", engDept, ceo);
        Position cfo = new Position(new PositionId("P-CFO"), "Chief Financial Officer", "Financial strategy, budgeting and compliance", finDept, ceo);
        Position cmo = new Position(new PositionId("P-CMO"), "Chief Marketing Officer", "Global marketing and customer acquisition strategy", mktDept, ceo);
        Position hrDir = new Position(new PositionId("P-HRDIR"), "VP of Human Resources", "People strategy, culture and global HR policy", hrDept, ceo);
        Position headDesign = new Position(new PositionId("P-VP-DES"), "VP of Product Design", "Product UX/UI and design systems", desDept, ceo);
        Position csMgr = new Position(new PositionId("P-CS-MGR"), "Customer Success Lead", "Customer satisfaction and retention operations", csDept, ceo);
        Position ciso = new Position(new PositionId("P-CISO"), "Chief Information Security Officer", "Global cybersecurity & risk management", secDept, ceo);
        Position headData = new Position(new PositionId("P-HEAD-DATA"), "Head of Data & AI", "AI strategy and data platform leadership", dataDept, cto);
        Position vpSales = new Position(new PositionId("P-VP-SALES"), "VP of Global Sales", "Enterprise revenue growth & account management", salesDept, ceo);
        positionRepository.saveAll(List.of(cto, cfo, cmo, hrDir, headDesign, csMgr, ciso, headData, vpSales));

        // Mid-Level & Specialist Positions
        Position principalArchitect = new Position(new PositionId("P-ARCH"), "Principal Cloud Architect", "Scalable multi-cloud systems architecture", engDept, cto);
        Position engLead = new Position(new PositionId("P-ENG-LEAD"), "Engineering Lead", "Managing software engineering teams", engDept, cto);
        Position srDev = new Position(new PositionId("P-SDEV"), "Senior Software Engineer", "Full-stack feature engineering", engDept, engLead);
        Position jrDev = new Position(new PositionId("P-JDEV"), "Junior Software Engineer", "Frontend web development & API integration", engDept, srDev);
        Position qaLead = new Position(new PositionId("P-QA-LEAD"), "QA Automation Lead", "End-to-end testing and quality engineering", qaDept, engLead);
        Position secAnalyst = new Position(new PositionId("P-SEC-ANALYST"), "Senior Cyber Security Analyst", "SOC monitoring, penetration testing & audit", secDept, ciso);
        Position dataEng = new Position(new PositionId("P-DATA-ENG"), "Senior Data Engineer", "ETL pipelines, big data & lakehouse systems", dataDept, headData);
        Position aiResearch = new Position(new PositionId("P-AI-SPEC"), "AI Research Scientist", "LLM modeling & algorithm optimization", rndDept, headData);
        Position hrManager = new Position(new PositionId("P-HR-MGR"), "Talent Acquisition Manager", "Recruiting engineering & corporate talent", hrDept, hrDir);
        Position hrSpec = new Position(new PositionId("P-HRSPEC"), "People Operations Specialist", "Employee onboarding, benefits and PTO admin", hrDept, hrManager);
        Position finAnalyst = new Position(new PositionId("P-FIN-ANALYST"), "Senior Financial Analyst", "Financial modeling and payroll audit", finDept, cfo);
        Position payrollSpec = new Position(new PositionId("P-PAYROLL-SPEC"), "Payroll Specialist", "Monthly payroll processing and tax reporting", finDept, finAnalyst);
        Position mktLead = new Position(new PositionId("P-MKT-LEAD"), "Growth Marketing Lead", "Digital performance marketing & SEO", mktDept, cmo);
        Position srDesigner = new Position(new PositionId("P-SR-DES"), "Senior Product Designer", "User experience and design systems", desDept, headDesign);
        Position legalCounsel = new Position(new PositionId("P-LEGAL-COUNSEL"), "Senior Corporate Counsel", "Contracts, IP protection & compliance", legalDept, cfo);
        positionRepository.saveAll(List.of(
            principalArchitect, engLead, srDev, jrDev, qaLead, secAnalyst, dataEng, aiResearch,
            hrManager, hrSpec, finAnalyst, payrollSpec, mktLead, srDesigner, legalCounsel
        ));

        // 3. Update Department Heads
        execDept.updateDetails(execDept.getName(), execDept.getDescription(), ceo);
        engDept.updateDetails(engDept.getName(), engDept.getDescription(), cto);
        hrDept.updateDetails(hrDept.getName(), hrDept.getDescription(), hrDir);
        mktDept.updateDetails(mktDept.getName(), mktDept.getDescription(), cmo);
        finDept.updateDetails(finDept.getName(), finDept.getDescription(), cfo);
        desDept.updateDetails(desDept.getName(), desDept.getDescription(), headDesign);
        csDept.updateDetails(csDept.getName(), csDept.getDescription(), csMgr);
        secDept.updateDetails(secDept.getName(), secDept.getDescription(), ciso);
        dataDept.updateDetails(dataDept.getName(), dataDept.getDescription(), headData);
        salesDept.updateDetails(salesDept.getName(), salesDept.getDescription(), vpSales);
        departmentRepository.saveAll(List.of(execDept, engDept, hrDept, mktDept, finDept, desDept, csDept, secDept, dataDept, salesDept));

        // 4. Create 25 Diverse Employees
        List<Employee> employees = new ArrayList<>();

        // Core Users & Leadership
        Employee alice = createEmp("Alice", "", "Smith", "alice@company.com", "5550100", "+1", 1979, 4, 12, 2012, 3, 1, Gender.WOMAN, Sex.FEMALE, "265000.00", MaritalStatus.MARRIED, ceo, EmployeeStatus.ACTIVE);
        Employee robert = createEmp("Robert", "James", "Chen", "robert.chen@techcorp.com", "5550101", "+1", 1982, 8, 21, 2014, 6, 15, Gender.MAN, Sex.MALE, "210000.00", MaritalStatus.MARRIED, cto, EmployeeStatus.ACTIVE);
        Employee frank = createEmp("Frank", "Thomas", "Castle", "hr@company.com", "5550102", "+1", 1990, 8, 30, 2020, 11, 1, Gender.MAN, Sex.MALE, "125000.00", MaritalStatus.SINGLE, hrDir, EmployeeStatus.ACTIVE);
        Employee charlie = createEmp("Charlie", "", "Brown", "manager@company.com", "5550103", "+1", 1988, 5, 12, 2018, 4, 10, Gender.MAN, Sex.MALE, "155000.00", MaritalStatus.MARRIED, engLead, EmployeeStatus.ACTIVE);
        Employee diana = createEmp("Diana", "Marie", "Prince", "user@company.com", "5550104", "+1", 1998, 11, 5, 2022, 1, 15, Gender.WOMAN, Sex.FEMALE, "92000.00", MaritalStatus.SINGLE, jrDev, EmployeeStatus.ACTIVE);

        employees.add(alice);
        employees.add(robert);
        employees.add(frank);
        employees.add(charlie);
        employees.add(diana);

        // Staff 6 to 20
        Employee marcus = createEmp("Marcus", "", "Vance", "marcus.vance@techcorp.com", "5550105", "+1", 1985, 2, 14, 2016, 9, 1, Gender.MAN, Sex.MALE, "185000.00", MaritalStatus.MARRIED, principalArchitect, EmployeeStatus.ACTIVE);
        Employee sophia = createEmp("Sophia", "", "Martinez", "sophia.martinez@techcorp.com", "5550106", "+1", 1991, 7, 19, 2019, 5, 20, Gender.WOMAN, Sex.FEMALE, "115000.00", MaritalStatus.SINGLE, qaLead, EmployeeStatus.ACTIVE);
        Employee priya = createEmp("Priya", "", "Patel", "priya.patel@techcorp.com", "5550107", "+1", 1993, 10, 8, 2021, 2, 1, Gender.WOMAN, Sex.FEMALE, "105000.00", MaritalStatus.MARRIED, hrManager, EmployeeStatus.ACTIVE);
        Employee david = createEmp("David", "", "Kim", "david.kim@techcorp.com", "5550108", "+1", 1995, 12, 3, 2022, 8, 15, Gender.MAN, Sex.MALE, "78000.00", MaritalStatus.SINGLE, hrSpec, EmployeeStatus.ACTIVE);
        Employee elena = createEmp("Elena", "", "Rostova", "elena.rostova@techcorp.com", "5550109", "+1", 1989, 4, 25, 2017, 11, 10, Gender.WOMAN, Sex.FEMALE, "140000.00", MaritalStatus.MARRIED, finAnalyst, EmployeeStatus.ACTIVE);
        Employee james = createEmp("James", "", "Wilson", "james.wilson@techcorp.com", "5550110", "+1", 1992, 9, 14, 2020, 3, 1, Gender.MAN, Sex.MALE, "110000.00", MaritalStatus.SINGLE, mktLead, EmployeeStatus.ACTIVE);
        Employee aaliyah = createEmp("Aaliyah", "", "Hassan", "aaliyah.hassan@techcorp.com", "5550111", "+1", 1994, 6, 30, 2021, 7, 12, Gender.WOMAN, Sex.FEMALE, "120000.00", MaritalStatus.SINGLE, srDesigner, EmployeeStatus.ACTIVE);
        Employee carlos = createEmp("Carlos", "", "Mendez", "carlos.mendez@techcorp.com", "5550112", "+1", 1987, 1, 11, 2015, 10, 1, Gender.MAN, Sex.MALE, "130000.00", MaritalStatus.MARRIED, csMgr, EmployeeStatus.ACTIVE);
        Employee kenji = createEmp("Kenji", "", "Sato", "kenji.sato@techcorp.com", "5550113", "+1", 1990, 3, 17, 2019, 1, 8, Gender.MAN, Sex.MALE, "138000.00", MaritalStatus.MARRIED, srDev, EmployeeStatus.ACTIVE);
        Employee emma = createEmp("Emma", "", "Watson", "emma.watson@techcorp.com", "5550114", "+1", 1997, 5, 22, 2023, 4, 1, Gender.WOMAN, Sex.FEMALE, "85000.00", MaritalStatus.SINGLE, jrDev, EmployeeStatus.ACTIVE);
        Employee sarah = createEmp("Sarah", "", "Jenkins", "sarah.jenkins@techcorp.com", "5550115", "+1", 1993, 11, 29, 2022, 10, 15, Gender.WOMAN, Sex.FEMALE, "82000.00", MaritalStatus.MARRIED, payrollSpec, EmployeeStatus.ACTIVE);
        Employee victor = createEmp("Victor", "", "Stone", "victor.stone@techcorp.com", "5550116", "+1", 1988, 9, 9, 2018, 5, 1, Gender.MAN, Sex.MALE, "195000.00", MaritalStatus.SINGLE, ciso, EmployeeStatus.ACTIVE);
        Employee nina = createEmp("Nina", "", "Simone", "nina.simone@techcorp.com", "5550117", "+1", 1986, 3, 15, 2017, 8, 1, Gender.WOMAN, Sex.FEMALE, "190000.00", MaritalStatus.MARRIED, headData, EmployeeStatus.ACTIVE);
        Employee lucas = createEmp("Lucas", "", "Muller", "lucas.muller@techcorp.com", "5550118", "+1", 1991, 12, 20, 2020, 2, 10, Gender.MAN, Sex.MALE, "145000.00", MaritalStatus.SINGLE, secAnalyst, EmployeeStatus.ACTIVE);
        Employee amara = createEmp("Amara", "", "Okonkwo", "amara.okonkwo@techcorp.com", "5550119", "+1", 1994, 4, 18, 2021, 9, 1, Gender.WOMAN, Sex.FEMALE, "150000.00", MaritalStatus.SINGLE, dataEng, EmployeeStatus.ACTIVE);
        Employee tariq = createEmp("Tariq", "", "Al-Mansoor", "tariq.almansoor@techcorp.com", "5550120", "+1", 1989, 7, 25, 2019, 11, 15, Gender.MAN, Sex.MALE, "175000.00", MaritalStatus.MARRIED, aiResearch, EmployeeStatus.ACTIVE);
        Employee hannah = createEmp("Hannah", "", "Abbott", "hannah.abbott@techcorp.com", "5550121", "+1", 1990, 10, 10, 2018, 6, 1, Gender.WOMAN, Sex.FEMALE, "160000.00", MaritalStatus.MARRIED, legalCounsel, EmployeeStatus.ACTIVE);

        employees.addAll(List.of(
            marcus, sophia, priya, david, elena, james, aaliyah, carlos, kenji, emma, sarah,
            victor, nina, lucas, amara, tariq, hannah
        ));

        // Staff 23 to 25: Status Edge Cases (Terminated, Resigned, On Leave)
        Employee tom = createEmp("Tom", "", "Hardy", "tom.hardy@techcorp.com", "5550122", "+1", 1986, 7, 7, 2017, 1, 1, Gender.MAN, Sex.MALE, "95000.00", MaritalStatus.SINGLE, srDev, EmployeeStatus.ACTIVE);
        tom.changeStatus(EmployeeStatus.RESIGNED);
        employees.add(tom);

        Employee kevin = createEmp("Kevin", "", "Bacon", "kevin.bacon@techcorp.com", "5550123", "+1", 1983, 12, 12, 2015, 6, 1, Gender.MAN, Sex.MALE, "88000.00", MaritalStatus.DIVORCED, jrDev, EmployeeStatus.ACTIVE);
        kevin.changeStatus(EmployeeStatus.TERMINATED);
        employees.add(kevin);

        Employee mei = createEmp("Mei", "", "Ling", "mei.ling@techcorp.com", "5550124", "+1", 1992, 8, 18, 2020, 9, 1, Gender.WOMAN, Sex.FEMALE, "98000.00", MaritalStatus.MARRIED, srDesigner, EmployeeStatus.ACTIVE);
        mei.changeStatus(EmployeeStatus.ON_LEAVE);
        employees.add(mei);

        employeeRepository.saveAll(employees);

        // 5. Seed 25 Employee Leave Balances (1 for each employee)
        List<EmployeeLeaveBalance> balances = new ArrayList<>();
        for (Employee emp : employees) {
            balances.add(new EmployeeLeaveBalance(emp, 12, 14));
        }
        leaveBalanceRepository.saveAll(balances);

        // 6. Seed 16 Realistic Leave Requests
        List<LeaveRequest> leaveRequests = new ArrayList<>();

        LeaveRequest req1 = new LeaveRequest(diana, LeaveType.ANNUAL, LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), "Vacation in Hawaii");
        req1.setStatus(LeaveStatus.PENDING_HR);
        req1.setManagerApprover(charlie);
        leaveRequests.add(req1);

        LeaveRequest req2 = new LeaveRequest(diana, LeaveType.SICK, LocalDate.now().minusDays(5), LocalDate.now().minusDays(4), "Flu recovery and medical rest");
        req2.setStatus(LeaveStatus.APPROVED);
        req2.setManagerApprover(charlie);
        req2.setHrApprover(frank);
        leaveRequests.add(req2);

        LeaveRequest req3 = new LeaveRequest(charlie, LeaveType.ANNUAL, LocalDate.now().plusDays(20), LocalDate.now().plusDays(25), "Family Trip to Europe");
        req3.setStatus(LeaveStatus.PENDING_HR);
        req3.setManagerApprover(alice);
        leaveRequests.add(req3);

        LeaveRequest req4 = new LeaveRequest(kenji, LeaveType.ANNUAL, LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), "Personal Matters");
        req4.setStatus(LeaveStatus.APPROVED);
        req4.setManagerApprover(charlie);
        req4.setHrApprover(frank);
        leaveRequests.add(req4);

        LeaveRequest req5 = new LeaveRequest(sophia, LeaveType.SICK, LocalDate.now().minusDays(15), LocalDate.now().minusDays(14), "Migraine treatment");
        req5.setStatus(LeaveStatus.APPROVED);
        req5.setManagerApprover(charlie);
        req5.setHrApprover(frank);
        leaveRequests.add(req5);

        LeaveRequest req6 = new LeaveRequest(marcus, LeaveType.ANNUAL, LocalDate.now().plusDays(40), LocalDate.now().plusDays(45), "Annual Leave Resort stay");
        req6.setStatus(LeaveStatus.PENDING_MANAGER);
        leaveRequests.add(req6);

        LeaveRequest req7 = new LeaveRequest(priya, LeaveType.MATERNITY, LocalDate.now().plusDays(5), LocalDate.now().plusDays(95), "Maternity Leave");
        req7.setStatus(LeaveStatus.APPROVED);
        req7.setManagerApprover(frank);
        req7.setHrApprover(frank);
        leaveRequests.add(req7);

        LeaveRequest req8 = new LeaveRequest(david, LeaveType.PATERNITY, LocalDate.now().minusDays(30), LocalDate.now().minusDays(20), "Paternity Leave");
        req8.setStatus(LeaveStatus.APPROVED);
        req8.setManagerApprover(priya);
        req8.setHrApprover(frank);
        leaveRequests.add(req8);

        LeaveRequest req9 = new LeaveRequest(elena, LeaveType.UNPAID, LocalDate.now().plusDays(15), LocalDate.now().plusDays(18), "Personal sabbatical extension");
        req9.setStatus(LeaveStatus.REJECTED);
        req9.setManagerApprover(robert);
        leaveRequests.add(req9);

        LeaveRequest req10 = new LeaveRequest(james, LeaveType.ANNUAL, LocalDate.now().plusDays(3), LocalDate.now().plusDays(5), "Marketing Conference & Break");
        req10.setStatus(LeaveStatus.APPROVED);
        req10.setManagerApprover(alice);
        req10.setHrApprover(frank);
        leaveRequests.add(req10);

        LeaveRequest req11 = new LeaveRequest(aaliyah, LeaveType.UNPAID, LocalDate.now().minusDays(40), LocalDate.now().minusDays(37), "Family Emergency Sabbatical");
        req11.setStatus(LeaveStatus.APPROVED);
        req11.setManagerApprover(alice);
        req11.setHrApprover(frank);
        leaveRequests.add(req11);

        LeaveRequest req12 = new LeaveRequest(carlos, LeaveType.ANNUAL, LocalDate.now().plusDays(30), LocalDate.now().plusDays(35), "Scuba Diving Expedition");
        req12.setStatus(LeaveStatus.PENDING_HR);
        req12.setManagerApprover(alice);
        leaveRequests.add(req12);

        LeaveRequest req13 = new LeaveRequest(victor, LeaveType.SICK, LocalDate.now().minusDays(10), LocalDate.now().minusDays(9), "Dental Surgery");
        req13.setStatus(LeaveStatus.APPROVED);
        req13.setManagerApprover(alice);
        req13.setHrApprover(frank);
        leaveRequests.add(req13);

        LeaveRequest req14 = new LeaveRequest(nina, LeaveType.ANNUAL, LocalDate.now().plusDays(12), LocalDate.now().plusDays(16), "Summer Vacation");
        req14.setStatus(LeaveStatus.CANCELLED);
        leaveRequests.add(req14);

        LeaveRequest req15 = new LeaveRequest(lucas, LeaveType.SICK, LocalDate.now().minusDays(2), LocalDate.now().minusDays(1), "High Fever & Cold");
        req15.setStatus(LeaveStatus.APPROVED);
        req15.setManagerApprover(victor);
        req15.setHrApprover(frank);
        leaveRequests.add(req15);

        LeaveRequest req16 = new LeaveRequest(amara, LeaveType.ANNUAL, LocalDate.now().plusDays(50), LocalDate.now().plusDays(55), "End of Year Holiday");
        req16.setStatus(LeaveStatus.PENDING_MANAGER);
        leaveRequests.add(req16);

        leaveRequestRepository.saveAll(leaveRequests);

        // 7. Seed 15 Leave Delegations
        List<LeaveDelegation> delegations = new ArrayList<>();
        delegations.add(new LeaveDelegation(alice, charlie, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30)));
        delegations.add(new LeaveDelegation(robert, marcus, LocalDate.now().plusDays(5), LocalDate.now().plusDays(20)));
        delegations.add(new LeaveDelegation(frank, priya, LocalDate.now().plusDays(10), LocalDate.now().plusDays(25)));
        delegations.add(new LeaveDelegation(charlie, kenji, LocalDate.now().plusDays(20), LocalDate.now().plusDays(35)));
        delegations.add(new LeaveDelegation(priya, david, LocalDate.now().plusDays(5), LocalDate.now().plusDays(95)));
        delegations.add(new LeaveDelegation(victor, lucas, LocalDate.now().plusDays(15), LocalDate.now().plusDays(30)));
        delegations.add(new LeaveDelegation(nina, amara, LocalDate.now().plusDays(12), LocalDate.now().plusDays(26)));
        delegations.add(new LeaveDelegation(carlos, diana, LocalDate.now().plusDays(30), LocalDate.now().plusDays(40)));
        delegations.add(new LeaveDelegation(marcus, kenji, LocalDate.now().plusDays(40), LocalDate.now().plusDays(50)));
        delegations.add(new LeaveDelegation(elena, sarah, LocalDate.now().plusDays(8), LocalDate.now().plusDays(18)));
        delegations.add(new LeaveDelegation(james, aaliyah, LocalDate.now().plusDays(3), LocalDate.now().plusDays(10)));
        delegations.add(new LeaveDelegation(tariq, amara, LocalDate.now().plusDays(2), LocalDate.now().plusDays(12)));
        delegations.add(new LeaveDelegation(sophia, diana, LocalDate.now().plusDays(14), LocalDate.now().plusDays(21)));
        delegations.add(new LeaveDelegation(hannah, frank, LocalDate.now().plusDays(18), LocalDate.now().plusDays(28)));
        delegations.add(new LeaveDelegation(david, priya, LocalDate.now().plusDays(22), LocalDate.now().plusDays(32)));
        leaveDelegationRepository.saveAll(delegations);

        // 8. Seed Attendance Records (Past 15 weekdays for active staff = 300+ records)
        for (Employee emp : employees) {
            if (emp.getStatus() == EmployeeStatus.ACTIVE) {
                for (int i = 0; i < 15; i++) {
                    LocalDate d = LocalDate.now().minusDays(i);
                    if (d.getDayOfWeek().getValue() < 6) { // Weekdays
                        AttendanceStatus status = (i == 2 && emp == diana) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
                        AttendanceRecord rec = new AttendanceRecord(emp, d, status);
                        
                        int checkInMin = (status == AttendanceStatus.LATE) ? 45 : (int) (Math.random() * 25);
                        rec.setCheckInTime(LocalTime.of(8, checkInMin));
                        
                        int checkOutMin = (int) (Math.random() * 30);
                        if (i % 3 == 1) { // Overtime days
                            rec.setOvertimeHours(new BigDecimal("2.0"));
                            rec.setCheckOutTime(LocalTime.of(19, 0));
                        } else {
                            rec.setCheckOutTime(LocalTime.of(17, checkOutMin));
                        }
                        attendanceRecordRepository.save(rec);
                    }
                }
            }
        }

        // 9. Seed 16 Overtime Requests
        List<OvertimeRequest> otRequests = new ArrayList<>();
        otRequests.add(new OvertimeRequest(diana, LocalDate.now().minusDays(1), new BigDecimal("2.5"), "Q3 Sprint Release Testing"));
        otRequests.add(new OvertimeRequest(kenji, LocalDate.now(), new BigDecimal("3.0"), "Cloud Infrastructure Upgrade & Migration"));
        otRequests.add(new OvertimeRequest(sophia, LocalDate.now().minusDays(3), new BigDecimal("2.0"), "Automated Regression Test Suite Run"));
        otRequests.add(new OvertimeRequest(marcus, LocalDate.now().minusDays(4), new BigDecimal("4.0"), "Disaster Recovery Drill & Failover Setup"));
        otRequests.add(new OvertimeRequest(lucas, LocalDate.now().minusDays(5), new BigDecimal("3.5"), "SOC Threat Hunting & Security Patching"));
        otRequests.add(new OvertimeRequest(amara, LocalDate.now().minusDays(6), new BigDecimal("2.0"), "ETL Pipeline Pipeline Optimization"));
        otRequests.add(new OvertimeRequest(tariq, LocalDate.now().minusDays(7), new BigDecimal("3.0"), "LLM Fine-Tuning & Model Evaluation"));
        otRequests.add(new OvertimeRequest(sarah, LocalDate.now().minusDays(8), new BigDecimal("2.5"), "Monthly Payroll Audit & Tax Processing"));
        otRequests.add(new OvertimeRequest(aaliyah, LocalDate.now().minusDays(9), new BigDecimal("1.5"), "Design System Component Review"));
        otRequests.add(new OvertimeRequest(james, LocalDate.now().minusDays(10), new BigDecimal("3.0"), "Q4 Marketing Campaign Launch"));
        otRequests.add(new OvertimeRequest(david, LocalDate.now().minusDays(11), new BigDecimal("2.0"), "New Employee Onboarding Orientation Prep"));
        otRequests.add(new OvertimeRequest(carlos, LocalDate.now().minusDays(12), new BigDecimal("4.0"), "Tier-1 Customer Escalation Resolution"));
        otRequests.add(new OvertimeRequest(elena, LocalDate.now().minusDays(13), new BigDecimal("3.5"), "Quarterly Budget Forecast Preparation"));
        otRequests.add(new OvertimeRequest(priya, LocalDate.now().minusDays(14), new BigDecimal("2.0"), "Executive Hiring Panel Interviewing"));
        otRequests.add(new OvertimeRequest(hannah, LocalDate.now().minusDays(15), new BigDecimal("3.0"), "Vendor Contract Compliance Audit"));
        otRequests.add(new OvertimeRequest(victor, LocalDate.now().minusDays(16), new BigDecimal("4.5"), "Critical Vulnerability Mitigation"));

        // Assign status variations
        otRequests.get(0).setStatus(OvertimeRequestStatus.APPROVED);
        otRequests.get(1).setStatus(OvertimeRequestStatus.PENDING_HR);
        otRequests.get(2).setStatus(OvertimeRequestStatus.APPROVED);
        otRequests.get(3).setStatus(OvertimeRequestStatus.APPROVED);
        otRequests.get(4).setStatus(OvertimeRequestStatus.PENDING_HR);
        otRequests.get(5).setStatus(OvertimeRequestStatus.APPROVED);
        otRequests.get(6).setStatus(OvertimeRequestStatus.REJECTED);
        otRequests.get(7).setStatus(OvertimeRequestStatus.APPROVED);
        otRequests.get(8).setStatus(OvertimeRequestStatus.APPROVED);
        otRequests.get(9).setStatus(OvertimeRequestStatus.PENDING_HR);
        otRequests.get(10).setStatus(OvertimeRequestStatus.APPROVED);
        otRequests.get(11).setStatus(OvertimeRequestStatus.APPROVED);
        otRequests.get(12).setStatus(OvertimeRequestStatus.APPROVED);
        otRequests.get(13).setStatus(OvertimeRequestStatus.PENDING_HR);
        otRequests.get(14).setStatus(OvertimeRequestStatus.REJECTED);
        otRequests.get(15).setStatus(OvertimeRequestStatus.APPROVED);

        overtimeRequestRepository.saveAll(otRequests);

        // 10. Seed 16 Finalized & Draft Payroll Runs (June 2025 - Sept 2026)
        if (frank != null) {
            String[] months = {
                "June 2025", "July 2025", "August 2025", "September 2025", "October 2025", "November 2025", "December 2025",
                "January 2026", "February 2026", "March 2026", "April 2026", "May 2026", "June 2026", "July 2026", "August 2026", "September 2026"
            };
            int[][] dates = {
                {2025, 6, 30}, {2025, 7, 31}, {2025, 8, 31}, {2025, 9, 30}, {2025, 10, 31}, {2025, 11, 30}, {2025, 12, 31},
                {2026, 1, 31}, {2026, 2, 28}, {2026, 3, 31}, {2026, 4, 30}, {2026, 5, 31}, {2026, 6, 30}, {2026, 7, 31}, {2026, 8, 31}, {2026, 9, 30}
            };

            for (int m = 0; m < months.length; m++) {
                int year = dates[m][0];
                int month = dates[m][1];
                int lastDay = dates[m][2];

                PayrollRun run = new PayrollRun(months[m], LocalDate.of(year, month, 1), LocalDate.of(year, month, lastDay), frank);
                for (Employee emp : employees) {
                    if (emp.getStatus() == EmployeeStatus.ACTIVE) {
                        PayrollItem item = new PayrollItem(emp);
                        item.setBaseSalary(emp.getSalary().getAmount());
                        payrollCalculationService.calculateTaxesAndDeductions(item);
                        run.addItem(item);
                    }
                }
                if (m == months.length - 1) {
                    run.setStatus(PayrollStatus.DRAFT);
                } else {
                    run.setStatus(PayrollStatus.FINALIZED);
                }
                payrollRunRepository.save(run);
            }
        }

        // 11. Seed 16 Initial Notifications
        if (notificationRepository.count() == 0 && diana != null && frank != null && kenji != null) {
            List<Notification> notifs = List.of(
                new Notification(String.valueOf(diana.getId()), "Your annual leave request for Vacation in Hawaii has been submitted.", NotificationType.LEAVE_SUBMITTED, "LEAVE-101", "/leave/requests"),
                new Notification(String.valueOf(frank.getId()), "New Overtime Request submitted by Kenji Sato for Cloud Infrastructure Upgrade.", NotificationType.OVERTIME_REQUEST, "OT-202", "/attendance/overtime"),
                new Notification(String.valueOf(diana.getId()), "August 2026 Payslip is now available for view and download.", NotificationType.GENERAL, "PAY-AUG2026", "/payroll/my-payslips"),
                new Notification(String.valueOf(frank.getId()), "System Audit Alert: New employee profile created for Emma Watson.", NotificationType.EMPLOYEE_ONBOARDED, "EMP-114", "/employees"),
                new Notification(String.valueOf(charlie.getId()), "Leave Request from Diana Prince approved by HR.", NotificationType.LEAVE_APPROVED_HR, "LEAVE-102", "/leave/requests"),
                new Notification(String.valueOf(kenji.getId()), "Overtime request for Cloud Infrastructure approved by HR.", NotificationType.OVERTIME_APPROVED, "OT-202", "/attendance/overtime"),
                new Notification(String.valueOf(alice.getId()), "Monthly Executive HR Summary for August 2026 generated.", NotificationType.SYSTEM_ANNOUNCEMENT, "REP-AUG2026", "/dashboard"),
                new Notification(String.valueOf(robert.getId()), "Quarterly Cloud Security Audit completed successfully.", NotificationType.GENERAL, "SEC-AUDIT-Q3", "/dashboard"),
                new Notification(String.valueOf(marcus.getId()), "New delegation rule assigned: Delegated to Kenji Sato.", NotificationType.DELEGATION_CREATED, "DEL-301", "/leave/delegations"),
                new Notification(String.valueOf(sophia.getId()), "Regression Test Suite Automation build passed.", NotificationType.GENERAL, "QA-BUILD-88", "/dashboard"),
                new Notification(String.valueOf(elena.getId()), "Finance Review for September Draft Payroll is ready.", NotificationType.GENERAL, "PAY-SEP2026", "/payroll/runs"),
                new Notification(String.valueOf(priya.getId()), "New Job Application received for Senior Backend Engineer.", NotificationType.GENERAL, "REC-801", "/employees"),
                new Notification(String.valueOf(victor.getId()), "SOC Security Threat Alert: All patches deployed.", NotificationType.SYSTEM_ANNOUNCEMENT, "SEC-PATCH-99", "/dashboard"),
                new Notification(String.valueOf(nina.getId()), "AI Platform Data Lakehouse migration scheduled.", NotificationType.GENERAL, "DATA-MIG-01", "/dashboard"),
                new Notification(String.valueOf(amara.getId()), "ETL Pipeline performance report available.", NotificationType.GENERAL, "ETL-REP-44", "/dashboard"),
                new Notification(String.valueOf(hannah.getId()), "Annual IP Legal Compliance review completed.", NotificationType.GENERAL, "LEG-COMP-2026", "/dashboard")
            );
            notificationRepository.saveAll(notifs);
        }

        System.out.println("Diverse and relevant dummy data seeded successfully with " + employees.size() + " employees across 15 departments and 25 positions!");
    }

    private void seedAuthData() {
        // Seed 15 Roles
        List<String> roleNames = Arrays.asList(
            "SUPER_ADMIN", "STANDARD_USER", "HR", "EXECUTIVE", "FINANCE",
            "MANAGER", "AUDITOR", "DEPT_HEAD", "RECRUITER", "PAYROLL_ADMIN",
            "ATTENDANCE_MANAGER", "LEAVE_APPROVER", "SECURITY_ADMIN", "COMPLIANCE_OFFICER", "IT_SUPPORT"
        );

        List<Role> seededRoles = new ArrayList<>();
        for (String rName : roleNames) {
            Role role = roleRepository.findByName(rName).orElseGet(() -> {
                Role newR = new Role();
                newR.setName(rName);
                newR.setDescription(rName.replace("_", " ") + " System Access Level");
                return roleRepository.save(newR);
            });
            seededRoles.add(role);
        }

        // Seed default permissions
        List<String> defaultPermissions = Arrays.asList(
                "employee:view", "employee:create", "employee:edit", "employee:delete",
                "department:view", "department:create", "department:edit", "department:delete",
                "position:view", "position:create", "position:edit", "position:delete",
                "company_profile:view", "company_profile:edit",
                "leave:view", "leave:approve", "leave:manage",
                "role:view", "role:create", "role:edit", "role:delete",
                "user:view", "user:create", "user:edit", "user:delete",
                "payroll:view", "payroll:run", "payroll:approve", "payroll:finalize",
                "attendance:view", "attendance:manage"
        );

        for (String permName : defaultPermissions) {
            if (permissionRepository.findByName(permName).isEmpty()) {
                permissionRepository.save(new Permission(permName));
            }
        }

        // Assign permissions to SUPER_ADMIN
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN").get();
        boolean roleUpdated = false;
        List<Permission> allPermissions = permissionRepository.findAll();
        for (Permission perm : allPermissions) {
            if (!superAdminRole.getPermissions().contains(perm)) {
                superAdminRole.getPermissions().add(perm);
                roleUpdated = true;
            }
        }
        if (roleUpdated) {
            roleRepository.save(superAdminRole);
        }

        // Seed STANDARD_USER
        Role standardUserRole = roleRepository.findByName("STANDARD_USER").get();
        List<String> stdPerms = Arrays.asList("employee:view", "company_profile:view", "leave:view");
        boolean stdRoleUpdated = false;
        for (String permName : stdPerms) {
            Permission perm = permissionRepository.findByName(permName).orElse(null);
            if (perm != null && !standardUserRole.getPermissions().contains(perm)) {
                standardUserRole.getPermissions().add(perm);
                stdRoleUpdated = true;
            }
        }
        if (stdRoleUpdated) {
            roleRepository.save(standardUserRole);
        }

        // Seed HR role
        Role hrRole = roleRepository.findByName("HR").get();
        List<String> hrPerms = Arrays.asList(
            "employee:view", "employee:create", "employee:edit", "employee:delete",
            "department:view", "department:create", "department:edit", "department:delete",
            "position:view", "position:create", "position:edit", "position:delete",
            "company_profile:view", "company_profile:edit",
            "leave:view", "leave:approve", "leave:manage",
            "payroll:view", "payroll:run", "payroll:finalize",
            "attendance:view", "attendance:manage"
        );
        boolean hrRoleUpdated = false;
        for (String permName : hrPerms) {
            Permission perm = permissionRepository.findByName(permName).orElse(null);
            if (perm != null && !hrRole.getPermissions().contains(perm)) {
                hrRole.getPermissions().add(perm);
                hrRoleUpdated = true;
            }
        }
        if (hrRoleUpdated) {
            roleRepository.save(hrRole);
        }

        // Seed EXECUTIVE role
        Role executiveRole = roleRepository.findByName("EXECUTIVE").get();
        List<String> execPerms = Arrays.asList("employee:view", "department:view", "position:view", "company_profile:view", "leave:view");
        boolean execRoleUpdated = false;
        for (String permName : execPerms) {
            Permission perm = permissionRepository.findByName(permName).orElse(null);
            if (perm != null && !executiveRole.getPermissions().contains(perm)) {
                executiveRole.getPermissions().add(perm);
                execRoleUpdated = true;
            }
        }
        if (execRoleUpdated) {
            roleRepository.save(executiveRole);
        }

        // Seed FINANCE role
        Role financeRole = roleRepository.findByName("FINANCE").get();
        List<String> finPerms = Arrays.asList(
            "employee:view", "department:view", "position:view",
            "company_profile:view", "leave:view",
            "payroll:view", "payroll:approve"
        );
        boolean finRoleUpdated = false;
        for (String permName : finPerms) {
            Permission perm = permissionRepository.findByName(permName).orElse(null);
            if (perm != null && !financeRole.getPermissions().contains(perm)) {
                financeRole.getPermissions().add(perm);
                finRoleUpdated = true;
            }
        }
        if (finRoleUpdated) {
            roleRepository.save(financeRole);
        }

        // Seed Ghost System Admin
        if (systemAdminRepository.findByEmail("suparadmin@company.com").isEmpty()) {
            model.auth.SystemAdmin ghostAdmin = new model.auth.SystemAdmin();
            ghostAdmin.setEmail("suparadmin@company.com");
            ghostAdmin.setPasswordHash(passwordEncoder.encode("suparadmin123"));
            ghostAdmin.getRoles().add(superAdminRole);
            systemAdminRepository.save(ghostAdmin);
            System.out.println("Ghost SuperAdmin seeded: suparadmin@company.com / suparadmin123");
        }

        // Assign Passwords and Roles to Employees
        setupUserCredentials("alice@company.com", "alice123", List.of(standardUserRole, executiveRole));
        setupUserCredentials("robert.chen@techcorp.com", "robert123", List.of(standardUserRole, financeRole));
        setupUserCredentials("hr@company.com", "hr123", List.of(hrRole));
        setupUserCredentials("manager@company.com", "manager123", List.of(standardUserRole));
        setupUserCredentials("user@company.com", "user123", List.of(standardUserRole));

        // Assign standard login for all remaining active staff
        List<Employee> allEmps = employeeRepository.findAll();
        for (Employee emp : allEmps) {
            if (emp.getPasswordHash() == null) {
                String pwd = emp.getFullName().getFirstName().toLowerCase() + "123";
                setupUserCredentials(emp.getEmail().getValue(), pwd, List.of(standardUserRole));
            }
        }
    }

    private void setupUserCredentials(String email, String rawPassword, List<Role> roles) {
        employeeRepository.findByEmail_Value(email).ifPresent(emp -> {
            emp.setPasswordHash(passwordEncoder.encode(rawPassword));
            emp.setRequiresPasswordChange(false);
            for (Role r : roles) {
                if (!emp.getRoles().contains(r)) {
                    emp.getRoles().add(r);
                }
            }
            employeeRepository.save(emp);
            System.out.println("User credential seeded: " + email + " / " + rawPassword);
        });
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
        if (status != EmployeeStatus.ACTIVE && status != null) {
            emp.changeStatus(status);
        }
        return emp;
    }
}
