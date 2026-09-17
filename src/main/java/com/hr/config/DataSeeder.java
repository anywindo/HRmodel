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
import repository.leave.EmployeeLeaveBalanceRepository;
import repository.leave.LeaveRequestRepository;

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
    private final CompanyProfileRepository companyProfileRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeLeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final repository.auth.SystemAdminRepository systemAdminRepository;
    private final repository.leave.LeaveDelegationRepository leaveDelegationRepository;

    public DataSeeder(DepartmentRepository departmentRepository, PositionRepository positionRepository, EmployeeRepository employeeRepository, CompanyProfileRepository companyProfileRepository, RoleRepository roleRepository, PermissionRepository permissionRepository, PasswordEncoder passwordEncoder, EmployeeLeaveBalanceRepository leaveBalanceRepository, LeaveRequestRepository leaveRequestRepository, repository.auth.SystemAdminRepository systemAdminRepository, repository.leave.LeaveDelegationRepository leaveDelegationRepository) {
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
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (companyProfileRepository.count() == 0) {
            seedCompanyProfile();
        }
        if (employeeRepository.count() == 0) {
            seedData();
        }
        seedAuthData();
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
                "TechCorp Solutions Inc.",
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
        employees.add(createEmp("Alice", "", "Smith", "alice@company.com", "1234567890", "+1", 1979, 4, 12, 2012, 3, 1, Gender.WOMAN, Sex.FEMALE, "265000.00", MaritalStatus.MARRIED, ceo, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Robert", "James", "Chen", "robert.chen@techcorp.com", "9876543210", "+1", 1982, 8, 21, 2014, 6, 15, Gender.MAN, Sex.MALE, "210000.00", MaritalStatus.MARRIED, cto, EmployeeStatus.ACTIVE));
        
        // Engineering & Product
        employees.add(createEmp("Diana", "Marie", "Prince", "user@company.com", "5559876543", "+1", 1998, 11, 5, 2022, 1, 15, Gender.WOMAN, Sex.FEMALE, "92000.00", MaritalStatus.SINGLE, jrDev, EmployeeStatus.ACTIVE));
        employees.add(createEmp("Charlie", "", "Brown", "manager@company.com", "5551234567", "+1", 1990, 5, 12, 2018, 4, 10, Gender.MAN, Sex.MALE, "150000.00", MaritalStatus.MARRIED, srDev, EmployeeStatus.ACTIVE));
        
        // HR, Marketing, Finance & Others
        employees.add(createEmp("Frank", "Thomas", "Castle", "hr@company.com", "5552221111", "+1", 1992, 8, 30, 2020, 11, 1, Gender.MAN, Sex.MALE, "88000.00", MaritalStatus.SINGLE, hrManager, EmployeeStatus.ACTIVE));

        // Some edge cases (terminated, resigned, etc.)
        Employee terminatedEmp = createEmp("Charlie", "", "Chaplin", "charlie.chaplin@company.com", "7700900111", "+44", 1980, 4, 16, 2010, 1, 1, Gender.MAN, Sex.MALE, "75000.00", MaritalStatus.DIVORCED, jrDev, EmployeeStatus.ACTIVE);
        terminatedEmp.changeStatus(model.employee.EmployeeStatus.TERMINATED);
        employees.add(terminatedEmp);

        Employee resignedEmp = createEmp("Diana", "", "Prince", "diana.prince@company.com", "2025550178", "+1", 1985, 3, 22, 2015, 6, 1, Gender.WOMAN, Sex.FEMALE, "95000.00", MaritalStatus.SINGLE, srDev, EmployeeStatus.ACTIVE);
        resignedEmp.changeStatus(model.employee.EmployeeStatus.RESIGNED);
        employees.add(resignedEmp);

        employeeRepository.saveAll(employees);
        
        // Seed Leave Balances for all employees
        List<EmployeeLeaveBalance> balances = new ArrayList<>();
        for (Employee emp : employees) {
            balances.add(new EmployeeLeaveBalance(emp, 12, 14));
        }
        leaveBalanceRepository.saveAll(balances);
        
        // Seed a mock Leave Request for Diana (the standard user)
        Employee diana = employees.stream().filter(e -> e.getEmail().getValue().equals("user@company.com")).findFirst().orElse(null);
        Employee charlie = employees.stream().filter(e -> e.getEmail().getValue().equals("manager@company.com")).findFirst().orElse(null);
        Employee alice = employees.stream().filter(e -> e.getEmail().getValue().equals("alice@company.com")).findFirst().orElse(null);
        Employee hr = employees.stream().filter(e -> e.getEmail().getValue().equals("hr@company.com")).findFirst().orElse(null);

        if (diana != null) {
            LeaveRequest req1 = new LeaveRequest(diana, LeaveType.ANNUAL, LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), "Vacation");
            leaveRequestRepository.save(req1);

            LeaveRequest req2 = new LeaveRequest(diana, LeaveType.SICK, LocalDate.now().minusDays(5), LocalDate.now().minusDays(4), "Flu");
            req2.setStatus(model.leave.LeaveStatus.APPROVED);
            req2.setManagerApprover(charlie);
            req2.setHrApprover(hr);
            leaveRequestRepository.save(req2);
        }

        if (charlie != null && alice != null) {
            LeaveRequest req3 = new LeaveRequest(charlie, LeaveType.ANNUAL, LocalDate.now().plusDays(20), LocalDate.now().plusDays(25), "Family Trip");
            req3.setStatus(model.leave.LeaveStatus.PENDING_HR);
            req3.setManagerApprover(alice);
            leaveRequestRepository.save(req3);
        }

        if (hr != null && alice != null) {
            LeaveRequest req4 = new LeaveRequest(hr, LeaveType.ANNUAL, LocalDate.now().plusDays(2), LocalDate.now().plusDays(3), "Personal errands");
            req4.setStatus(model.leave.LeaveStatus.REJECTED);
            req4.setManagerApprover(alice);
            leaveRequestRepository.save(req4);
        }

        // Seed some Leave Delegations
        if (alice != null && charlie != null) {
            model.leave.LeaveDelegation delegation = new model.leave.LeaveDelegation(alice, charlie, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
            leaveDelegationRepository.save(delegation);
        }

        System.out.println("Enhanced diverse dummy data seeded successfully with " + employees.size() + " employees across 6 departments and 16 positions!");
    }

    private void seedAuthData() {
        // Seed default permissions
        List<String> defaultPermissions = java.util.Arrays.asList(
                "employee:view", "employee:create", "employee:edit", "employee:delete",
                "department:view", "department:create", "department:edit", "department:delete",
                "position:view", "position:create", "position:edit", "position:delete",
                "company_profile:view", "company_profile:edit",
                "leave:view", "leave:approve", "leave:manage",
                "role:view", "role:create", "role:edit", "role:delete",
                "user:view", "user:create", "user:edit", "user:delete"
        );

        for (String permName : defaultPermissions) {
            if (permissionRepository.findByName(permName).isEmpty()) {
                permissionRepository.save(new Permission(permName));
            }
        }

        // Seed SUPER_ADMIN role
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.setName("SUPER_ADMIN");
            role.setDescription("Super Administrator with full access");
            return roleRepository.save(role);
        });

        // Assign all permissions to SUPER_ADMIN
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

        // Seed STANDARD_USER role
        Role standardUserRole = roleRepository.findByName("STANDARD_USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("STANDARD_USER");
            role.setDescription("Standard Employee with read-only access to company data");
            return roleRepository.save(role);
        });

        // Assign limited permissions to STANDARD_USER
        List<String> stdPerms = java.util.Arrays.asList(
            "employee:view", "company_profile:view", "leave:view"
        );
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
        Role hrRole = roleRepository.findByName("HR").orElseGet(() -> {
            Role role = new Role();
            role.setName("HR");
            role.setDescription("Human Resources with employee management and leave approval access");
            return roleRepository.save(role);
        });

        List<String> hrPerms = java.util.Arrays.asList(
            "employee:view", "employee:create", "employee:edit", "employee:delete",
            "department:view", "department:create", "department:edit", "department:delete",
            "position:view", "position:create", "position:edit", "position:delete",
            "company_profile:view", "company_profile:edit",
            "leave:view", "leave:approve", "leave:manage"
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
        Role executiveRole = roleRepository.findByName("EXECUTIVE").orElseGet(() -> {
            Role role = new Role();
            role.setName("EXECUTIVE");
            role.setDescription("Executive Leadership with high-level dashboard access");
            return roleRepository.save(role);
        });

        // Assign permissions to EXECUTIVE (same as HR basically, or standard + company view)
        boolean execRoleUpdated = false;
        List<String> execPerms = java.util.Arrays.asList(
            "employee:view", "department:view", "position:view",
            "company_profile:view", "leave:view"
        );
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

        // Assign Roles and Passwords to Employees
        
        // Seed Ghost System Admin
        if (systemAdminRepository.findByEmail("suparadmin@company.com").isEmpty()) {
            model.auth.SystemAdmin ghostAdmin = new model.auth.SystemAdmin();
            ghostAdmin.setEmail("suparadmin@company.com");
            ghostAdmin.setPasswordHash(passwordEncoder.encode("suparadmin123"));
            ghostAdmin.getRoles().add(superAdminRole);
            systemAdminRepository.save(ghostAdmin);
            System.out.println("Ghost SuperAdmin seeded: suparadmin@company.com / suparadmin123");
        }

        employeeRepository.findByEmail_Value("alice@company.com").ifPresent(alice -> {
            alice.setPasswordHash(passwordEncoder.encode("alice123"));
            alice.setRequiresPasswordChange(false);
            alice.getRoles().add(standardUserRole);
            alice.getRoles().add(executiveRole);
            employeeRepository.save(alice);
            System.out.println("CEO seeded: alice@company.com / alice123 (Alice Smith - CEO)");
        });

        employeeRepository.findByEmail_Value("hr@company.com").ifPresent(frank -> {
            frank.setPasswordHash(passwordEncoder.encode("hr123"));
            frank.setRequiresPasswordChange(false);
            frank.getRoles().add(hrRole);
            employeeRepository.save(frank);
            System.out.println("HR user seeded: hr@company.com / hr123 (Frank Castle - HR Manager)");
        });

        employeeRepository.findByEmail_Value("manager@company.com").ifPresent(charlie -> {
            charlie.setPasswordHash(passwordEncoder.encode("manager123"));
            charlie.setRequiresPasswordChange(false);
            charlie.getRoles().add(standardUserRole);
            employeeRepository.save(charlie);
            System.out.println("Manager user seeded: manager@company.com / manager123 (Charlie Brown - Sr. Software Engineer)");
        });

        employeeRepository.findByEmail_Value("user@company.com").ifPresent(diana -> {
            diana.setPasswordHash(passwordEncoder.encode("user123"));
            diana.setRequiresPasswordChange(false);
            diana.getRoles().add(standardUserRole);
            employeeRepository.save(diana);
            System.out.println("Standard user seeded: user@company.com / user123 (Diana Prince - Jr. Software Engineer)");
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
