package service.rbac;

import com.hr.dto.rbac.*;
import model.auth.Permission;
import model.auth.Role;

import model.employee.Employee;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.auth.PermissionRepository;
import repository.auth.RoleRepository;
import repository.employee.EmployeeRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RbacService {

    private static final Set<String> SYSTEM_ROLES = Set.of("SUPER_ADMIN", "STANDARD_USER", "HR");
    private static final Set<String> PROTECTED_USERS = Set.of("suparadmin@company.com");

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final service.notification.NotificationService notificationService;

    public RbacService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       EmployeeRepository employeeRepository,
                       PasswordEncoder passwordEncoder,
                       service.notification.NotificationService notificationService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    // --- PERMISSIONS ---
    @Transactional(readOnly = true)
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(Permission::getName))
                .map(p -> new PermissionDTO(p.getId(), p.getName()))
                .collect(Collectors.toList());
    }

    // --- ROLES ---
    @Transactional(readOnly = true)
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .sorted(Comparator.comparing(Role::getId))
                .map(this::toRoleResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponseDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
        return toRoleResponseDTO(role);
    }

    @Transactional
    public RoleResponseDTO createRole(RoleRequestDTO req) {
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name is required");
        }
        String cleanName = req.getName().trim().toUpperCase();
        if (roleRepository.findByName(cleanName).isPresent()) {
            throw new IllegalArgumentException("Role with name '" + cleanName + "' already exists");
        }

        Role role = new Role();
        role.setName(cleanName);
        role.setDescription(req.getDescription());

        if (req.getPermissionIds() != null && !req.getPermissionIds().isEmpty()) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(req.getPermissionIds()));
            role.setPermissions(perms);
        }

        Role saved = roleRepository.save(role);
        return toRoleResponseDTO(saved);
    }

    @Transactional
    public RoleResponseDTO updateRole(Long id, RoleRequestDTO req) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));

        boolean isSystemRole = SYSTEM_ROLES.contains(role.getName());

        if (!isSystemRole && req.getName() != null && !req.getName().trim().isEmpty()) {
            String cleanName = req.getName().trim().toUpperCase();
            if (!cleanName.equals(role.getName()) && roleRepository.findByName(cleanName).isPresent()) {
                throw new IllegalArgumentException("Role with name '" + cleanName + "' already exists");
            }
            role.setName(cleanName);
        }

        if (req.getDescription() != null) {
            role.setDescription(req.getDescription());
        }

        if (req.getPermissionIds() != null) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(req.getPermissionIds()));
            if ("SUPER_ADMIN".equals(role.getName())) {
                permissionRepository.findAll().stream()
                        .filter(p -> p.getName().startsWith("role:") || p.getName().startsWith("user:"))
                        .forEach(perms::add);
            }
            role.setPermissions(perms);
        }

        Role saved = roleRepository.save(role);
        return toRoleResponseDTO(saved);
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));

        if (SYSTEM_ROLES.contains(role.getName())) {
            throw new IllegalArgumentException("System role '" + role.getName() + "' cannot be deleted");
        }

        long usersCount = employeeRepository.countByRoles_Id(id);
        if (usersCount > 0) {
            throw new IllegalArgumentException("Cannot delete role '" + role.getName() + "' because it is assigned to " + usersCount + " employee(s)");
        }

        roleRepository.delete(role);
    }

    // --- USERS ---
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return employeeRepository.findAll().stream()
                .sorted(Comparator.comparing(Employee::getId))
                .map(this::toUserResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
        return toUserResponseDTO(emp);
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO req) {
        throw new UnsupportedOperationException("Users are now created and managed via the Employee management module.");
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO req) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));

        if (req.getEmail() != null && !req.getEmail().trim().isEmpty()) {
            String cleanEmail = req.getEmail().trim().toLowerCase();
            if (!cleanEmail.equalsIgnoreCase(employee.getEmail().getValue()) && employeeRepository.findByEmail_Value(cleanEmail).isPresent()) {
                throw new IllegalArgumentException("Email '" + cleanEmail + "' is already in use");
            }
            employee.changeEmail(new model.employee.Email(cleanEmail));
        }

        if (req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
            if (req.getPassword().trim().length() < 6) {
                throw new IllegalArgumentException("New password must be at least 6 characters");
            }
            employee.setPasswordHash(passwordEncoder.encode(req.getPassword().trim()));
        }

        if (req.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(req.getRoleIds()));
            if (PROTECTED_USERS.contains(employee.getEmail().getValue())) {
                roleRepository.findByName("SUPER_ADMIN").ifPresent(roles::add);
            }
            employee.setRoles(roles);
        }

        Employee saved = employeeRepository.save(employee);
        
        String roleNames = saved.getRoles().stream().map(Role::getName).collect(Collectors.joining(", "));
        notificationService.createNotification(
            saved.getEmployeeId(),
            "Your system roles have been updated to: " + roleNames + ".",
            model.notification.NotificationType.ROLE_ASSIGNED,
            String.valueOf(saved.getId()),
            "/profile"
        );
        
        return toUserResponseDTO(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        throw new UnsupportedOperationException("Users are now deleted via the Employee management module.");
    }

    private RoleResponseDTO toRoleResponseDTO(Role role) {
        RoleResponseDTO dto = new RoleResponseDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setSystemRole(SYSTEM_ROLES.contains(role.getName()));
        dto.setUserCount((int) employeeRepository.countByRoles_Id(role.getId()));

        List<PermissionDTO> perms = role.getPermissions().stream()
                .sorted(Comparator.comparing(Permission::getName))
                .map(p -> new PermissionDTO(p.getId(), p.getName()))
                .collect(Collectors.toList());
        dto.setPermissions(perms);
        return dto;
    }

    private UserResponseDTO toUserResponseDTO(Employee emp) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(emp.getId());
        dto.setEmail(emp.getEmail().getValue());
        dto.setActive(emp.getStatus() != model.employee.EmployeeStatus.TERMINATED);
        dto.setEmployeeId(emp.getEmployeeId());

        if (emp.getFullName() != null) {
            dto.setEmployeeName(emp.getFullName().getFirstName() + " " + emp.getFullName().getLastName());
        }
        if (emp.getPosition() != null) {
            if (emp.getPosition().getTitle() != null) {
                dto.setPositionTitle(emp.getPosition().getTitle());
            }
            if (emp.getPosition().getDepartment() != null) {
                dto.setDepartmentName(emp.getPosition().getDepartment().getName());
            }
        }

        List<RoleSummaryDTO> roles = emp.getRoles().stream()
                .sorted(Comparator.comparing(Role::getId))
                .map(r -> new RoleSummaryDTO(r.getId(), r.getName(), r.getDescription()))
                .collect(Collectors.toList());
        dto.setRoles(roles);

        return dto;
    }
}
