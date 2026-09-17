package service.employee;

import com.hr.dto.EmployeeRequest;
import com.hr.dto.EmployeeResponse;
import model.employee.*;
import model.position.Position;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.position.PositionRepository;
import repository.employee.EmployeeRepository;
import repository.auth.RoleRepository;
import model.leave.EmployeeLeaveBalance;
import repository.leave.EmployeeLeaveBalanceRepository;
import model.leave.LeaveDelegation;
import repository.leave.LeaveDelegationRepository;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Set;

import service.notification.NotificationService;
import model.notification.NotificationType;
import service.email.EmailService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final EmployeeLeaveBalanceRepository balanceRepository;
    private final RoleRepository roleRepository;
    private final LeaveDelegationRepository leaveDelegationRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, PositionRepository positionRepository, EmployeeLeaveBalanceRepository balanceRepository, RoleRepository roleRepository, LeaveDelegationRepository leaveDelegationRepository, NotificationService notificationService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.positionRepository = positionRepository;
        this.balanceRepository = balanceRepository;
        this.roleRepository = roleRepository;
        this.leaveDelegationRepository = leaveDelegationRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean canSeeAll = auth != null && auth.getAuthorities().stream().anyMatch(a -> 
            a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_EXECUTIVE"));
        
        List<Employee> allEmployees = employeeRepository.findAll();
        
        List<LeaveDelegation> activeDelegations = leaveDelegationRepository.findAllActiveDelegations(LocalDate.now());
        Set<String> delegatorIds = activeDelegations.stream().map(d -> d.getDelegator().getEmployeeId()).collect(Collectors.toSet());
        Set<String> delegateeIds = activeDelegations.stream().map(d -> d.getDelegatee().getEmployeeId()).collect(Collectors.toSet());

        if (canSeeAll) {
            return allEmployees.stream().map(e -> new EmployeeResponse(e, allEmployees, delegatorIds.contains(e.getEmployeeId()), delegateeIds.contains(e.getEmployeeId()))).collect(Collectors.toList());
        }
        
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return List.of();
        }
        
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        Employee myEmp = employeeRepository.findByEmail_Value(userDetails.getUsername()).orElse(null);
                
        if (myEmp == null) {
            return List.of();
        }
        
        String myDeptId = myEmp.getPosition() != null && myEmp.getPosition().getDepartment() != null 
            ? myEmp.getPosition().getDepartment().getDepartmentId().getValue() : null;
        Position myManagerPos = myEmp.getPosition() != null ? myEmp.getPosition().getReportsTo() : null;
        
        List<Employee> filtered = allEmployees.stream().filter(e -> {
            if (e.getStatus() != EmployeeStatus.ACTIVE) return false;
            
            if (e.getEmployeeId().equals(myEmp.getEmployeeId())) return true;
            if (myDeptId != null && e.getPosition() != null && e.getPosition().getDepartment() != null && 
                e.getPosition().getDepartment().getDepartmentId().getValue().equals(myDeptId)) {
                return true;
            }
            if (myManagerPos != null && e.getPosition() != null && 
                e.getPosition().getPositionId().equals(myManagerPos.getPositionId())) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        return filtered.stream().map(e -> new EmployeeResponse(e, allEmployees, delegatorIds.contains(e.getEmployeeId()), delegateeIds.contains(e.getEmployeeId()))).collect(Collectors.toList());
    }

    public List<EmployeeResponse> getEmployeesByDepartmentId(String departmentId) {
        List<Employee> allEmployees = employeeRepository.findAll();
        List<Employee> filtered = employeeRepository.findByPosition_Department_DepartmentIdValue(departmentId.toUpperCase());
        
        List<LeaveDelegation> activeDelegations = leaveDelegationRepository.findAllActiveDelegations(LocalDate.now());
        Set<String> delegatorIds = activeDelegations.stream().map(d -> d.getDelegator().getEmployeeId()).collect(Collectors.toSet());
        Set<String> delegateeIds = activeDelegations.stream().map(d -> d.getDelegatee().getEmployeeId()).collect(Collectors.toSet());

        return filtered.stream().map(e -> new EmployeeResponse(e, allEmployees, delegatorIds.contains(e.getEmployeeId()), delegateeIds.contains(e.getEmployeeId()))).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        List<LeaveDelegation> activeDelegations = leaveDelegationRepository.findAllActiveDelegations(LocalDate.now());
        Set<String> delegatorIds = activeDelegations.stream().map(d -> d.getDelegator().getEmployeeId()).collect(Collectors.toSet());
        Set<String> delegateeIds = activeDelegations.stream().map(d -> d.getDelegatee().getEmployeeId()).collect(Collectors.toSet());

        return new EmployeeResponse(employee, employeeRepository.findAll(), delegatorIds.contains(employee.getEmployeeId()), delegateeIds.contains(employee.getEmployeeId()));
    }

    @Transactional
    @SuppressWarnings("null")
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        FullName fullName = new FullName(request.getFirstName(), request.getMiddleName(), request.getLastName());
        Email email = new Email(request.getEmail());
        PhoneNumber phone = new PhoneNumber(request.getPhoneNumber(), request.getPhoneCountryCode());
        Salary salary = new Salary(request.getSalaryAmount());
        
        Gender gender = Gender.valueOf(request.getGender().toUpperCase());
        Sex sex = Sex.valueOf(request.getSex().toUpperCase());
        MaritalStatus maritalStatus = request.getMaritalStatus() != null ? MaritalStatus.valueOf(request.getMaritalStatus().toUpperCase()) : MaritalStatus.PREFER_NOT_TO_SAY;

        model.position.Position position = null;
        if (request.getPositionId() != null && !request.getPositionId().isBlank()) {
            position = positionRepository.findByPositionId(new model.position.PositionId(request.getPositionId().toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException("Position not found"));
        }

        // Validate via static factory in Domain Model
        Employee newEmployee = Employee.create(
                fullName, email, phone, 
                request.getDateOfBirth(), request.getHireDate(), 
                gender, sex, salary, maritalStatus, position
        );

        // Auto-assign role and default password
        model.auth.Role standardRole = roleRepository.findByName("STANDARD_USER")
                .orElseGet(() -> roleRepository.findByName("USER").orElse(null));
        if (standardRole != null) {
            newEmployee.getRoles().add(standardRole);
        }
        
        // Use a default hashed password for newly created employees (DOB in DDMMYYYY format)
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy");
        String defaultPassword = newEmployee.getDateOfBirth().format(formatter);
        newEmployee.setPasswordHash(passwordEncoder.encode(defaultPassword));

        Employee saved = employeeRepository.save(newEmployee);
        
        // Initialize leave balance
        EmployeeLeaveBalance initialBalance = new EmployeeLeaveBalance(saved, 12, 14);
        balanceRepository.save(initialBalance);
        
        String token = "auto-activated";

        // Notify HR and SuperAdmin about onboarding
        String posTitle = position != null ? position.getTitle() : "Unassigned";
        String deptTitle = (position != null && position.getDepartment() != null) ? " in " + position.getDepartment().getName() : "";
        String onboardMsg = "New employee " + saved.getFullName().getFirstName() + " " + saved.getFullName().getLastName() + " has been onboarded (" + posTitle + deptTitle + ").";
        notificationService.notifyRole("HR", onboardMsg, NotificationType.EMPLOYEE_ONBOARDED, saved.getEmployeeId(), "/employees");
        notificationService.notifyRole("SUPER_ADMIN", onboardMsg, NotificationType.EMPLOYEE_ONBOARDED, saved.getEmployeeId(), "/employees");

        // Notify reporting manager if any
        if (position != null && position.getReportsTo() != null) {
            String mgrPosId = position.getReportsTo().getPositionId().getValue();
            employeeRepository.findByPosition_PositionId_Value(mgrPosId).forEach(mgr -> {
                notificationService.createNotification(
                    mgr.getEmployeeId(),
                    "New team member " + saved.getFullName().getFirstName() + " " + saved.getFullName().getLastName() + " has joined your team as " + posTitle + ".",
                    NotificationType.EMPLOYEE_ONBOARDED,
                    saved.getEmployeeId(),
                    "/employees"
                );
            });
        }
        
        return new EmployeeResponse(saved, token);
    }

    @Transactional
    public EmployeeResponse updateEmployee(String employeeId, EmployeeRequest request) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new IllegalStateException("Cannot edit a terminated employee");
        }

        if (request.getEmail() != null) {
            employee.changeEmail(new Email(request.getEmail()));
        }
        if (request.getSalaryAmount() != null) {
            employee.updateSalary(new Salary(request.getSalaryAmount()));
        }
        
        FullName fullName = null;
        if (request.getFirstName() != null && request.getLastName() != null) {
            fullName = new FullName(request.getFirstName(), request.getMiddleName(), request.getLastName());
        }
        
        PhoneNumber phone = null;
        if (request.getPhoneNumber() != null && request.getPhoneCountryCode() != null) {
            phone = new PhoneNumber(request.getPhoneNumber(), request.getPhoneCountryCode());
        }
        
        Gender gender = request.getGender() != null ? Gender.valueOf(request.getGender().toUpperCase()) : null;
        Sex sex = request.getSex() != null ? Sex.valueOf(request.getSex().toUpperCase()) : null;
        EmployeeStatus status = request.getStatus() != null ? EmployeeStatus.valueOf(request.getStatus().toUpperCase()) : null;
        MaritalStatus maritalStatus = request.getMaritalStatus() != null ? MaritalStatus.valueOf(request.getMaritalStatus().toUpperCase()) : null;

        model.position.Position position = null;
        if (request.getPositionId() != null && !request.getPositionId().isBlank()) {
            position = positionRepository.findByPositionId(new model.position.PositionId(request.getPositionId().toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException("Position not found"));
        }

        boolean posChanged = position != null && (employee.getPosition() == null || !employee.getPosition().getPositionId().equals(position.getPositionId()));

        employee.updateDetails(
            fullName, 
            phone, 
            gender, 
            sex, 
            status, 
            request.getDateOfBirth(), 
            request.getHireDate(),
            maritalStatus,
            position
        );

        Employee saved = employeeRepository.save(employee);

        // User active status is now driven solely by EmployeeStatus, so no separate User update is needed.

        // Notify employee about profile update
        notificationService.createNotification(
            saved.getEmployeeId(),
            "Your employee profile details have been updated by HR.",
            NotificationType.EMPLOYEE_PROFILE_UPDATED,
            saved.getEmployeeId(),
            "/profile"
        );

        if (posChanged) {
            String deptSuffix = (position.getDepartment() != null) ? " in " + position.getDepartment().getName() : "";
            notificationService.createNotification(
                saved.getEmployeeId(),
                "Your job assignment has been updated to: " + position.getTitle() + deptSuffix + ".",
                NotificationType.POSITION_ASSIGNED,
                position.getPositionId().getValue(),
                "/profile"
            );
        }

        return new EmployeeResponse(saved);
    }

    @Transactional
    public void changeEmployeeStatus(String employeeId, String statusString, String reason) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        
        EmployeeStatus newStatus = EmployeeStatus.fromString(statusString);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            if (employee.getEmail().getValue().equals(userDetails.getUsername()) && newStatus == EmployeeStatus.TERMINATED) {
                throw new IllegalStateException("You cannot terminate your own account");
            }
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("A reason is required for status changes");
        }

        employee.changeStatus(newStatus);
        employeeRepository.save(employee);

        String termMsg = "Employee " + employee.getFullName().getFirstName() + " " + employee.getFullName().getLastName() + " status has been changed to " + newStatus.getDisplayName() + ".";
        notificationService.notifyRole("HR", termMsg, NotificationType.EMPLOYEE_STATUS_CHANGED, employee.getEmployeeId(), "/employees");
        
        String emailSubject;
        String actionText;
        switch (newStatus) {
            case TERMINATED -> {
                emailSubject = "Notice of Employment Termination";
                actionText = "formally notify you that your employment has been terminated";
            }
            case SUSPENDED -> {
                emailSubject = "Notice of Suspension";
                actionText = "formally notify you that you have been suspended";
            }
            case RESIGNED -> {
                emailSubject = "Acceptance of Resignation";
                actionText = "acknowledge and accept your resignation";
            }
            case RETIRED -> {
                emailSubject = "Notice of Retirement";
                actionText = "confirm your retirement";
            }
            default -> {
                emailSubject = "Notice of Status Change";
                actionText = "notify you that your employment status has been changed to " + newStatus.getDisplayName();
            }
        }

        // Send email to the employee
        String emailBody = "Dear " + employee.getFullName().getFirstName() + ",\n\n" +
                "This email is to " + actionText + ".\n\n" +
                "Reason provided:\n" + reason + "\n\n" +
                "Please contact HR if you have any questions.\n\n" +
                "Sincerely,\nHuman Resources";
        emailService.sendEmail(employee.getEmail().getValue(), emailSubject, emailBody);
    }
}
