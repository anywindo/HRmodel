package service.rbac;

import com.hr.dto.rbac.*;
import model.auth.Permission;
import model.auth.Role;
import model.auth.User;
import model.employee.Employee;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.auth.PermissionRepository;
import repository.auth.RoleRepository;
import repository.auth.UserRepository;
import repository.employee.EmployeeRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RbacService {

    private static final Set<String> SYSTEM_ROLES = Set.of("SUPER_ADMIN", "STANDARD_USER", "HR");
    private static final Set<String> PROTECTED_USERS = Set.of("suparadmin@company.com");

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public RbacService(RoleRepository roleRepository,
                       UserRepository userRepository,
                       PermissionRepository permissionRepository,
                       EmployeeRepository employeeRepository,
                       PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
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

        long usersCount = userRepository.countByRoles_Id(id);
        if (usersCount > 0) {
            throw new IllegalArgumentException("Cannot delete role '" + role.getName() + "' because it is assigned to " + usersCount + " user(s)");
        }

        roleRepository.delete(role);
    }

    // --- USERS ---
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(this::toUserResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return toUserResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO req) {
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        String cleanEmail = req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("User with email '" + cleanEmail + "' already exists");
        }

        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        User user = new User();
        user.setEmail(cleanEmail);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setActive(req.getIsActive() != null ? req.getIsActive() : true);

        if (req.getEmployeeId() != null && !req.getEmployeeId().trim().isEmpty()) {
            Employee emp = employeeRepository.findByEmployeeId(req.getEmployeeId().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + req.getEmployeeId()));
            user.setEmployee(emp);
        }

        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(req.getRoleIds()));
            user.setRoles(roles);
        }

        User saved = userRepository.save(user);
        return toUserResponseDTO(saved);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (req.getEmail() != null && !req.getEmail().trim().isEmpty()) {
            String cleanEmail = req.getEmail().trim().toLowerCase();
            if (!cleanEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(cleanEmail)) {
                throw new IllegalArgumentException("Email '" + cleanEmail + "' is already in use");
            }
            user.setEmail(cleanEmail);
        }

        if (req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
            if (req.getPassword().trim().length() < 6) {
                throw new IllegalArgumentException("New password must be at least 6 characters");
            }
            user.setPasswordHash(passwordEncoder.encode(req.getPassword().trim()));
        }

        if (req.getIsActive() != null) {
            if (!req.getIsActive() && PROTECTED_USERS.contains(user.getEmail())) {
                throw new IllegalArgumentException("Primary superadmin account cannot be deactivated");
            }
            user.setActive(req.getIsActive());
        }

        if (req.getEmployeeId() != null) {
            if (req.getEmployeeId().trim().isEmpty()) {
                user.setEmployee(null);
            } else {
                Employee emp = employeeRepository.findByEmployeeId(req.getEmployeeId().trim())
                        .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + req.getEmployeeId()));
                user.setEmployee(emp);
            }
        }

        if (req.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(req.getRoleIds()));
            if (PROTECTED_USERS.contains(user.getEmail())) {
                roleRepository.findByName("SUPER_ADMIN").ifPresent(roles::add);
            }
            user.setRoles(roles);
        }

        User saved = userRepository.save(user);
        return toUserResponseDTO(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (PROTECTED_USERS.contains(user.getEmail())) {
            throw new IllegalArgumentException("Primary superadmin account cannot be deleted");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && user.getEmail().equalsIgnoreCase(auth.getName())) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }

        userRepository.delete(user);
    }

    private RoleResponseDTO toRoleResponseDTO(Role role) {
        RoleResponseDTO dto = new RoleResponseDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setSystemRole(SYSTEM_ROLES.contains(role.getName()));
        dto.setUserCount((int) userRepository.countByRoles_Id(role.getId()));

        List<PermissionDTO> perms = role.getPermissions().stream()
                .sorted(Comparator.comparing(Permission::getName))
                .map(p -> new PermissionDTO(p.getId(), p.getName()))
                .collect(Collectors.toList());
        dto.setPermissions(perms);
        return dto;
    }

    private UserResponseDTO toUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setActive(user.isActive());

        if (user.getEmployee() != null) {
            Employee emp = user.getEmployee();
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
        }

        List<RoleSummaryDTO> roles = user.getRoles().stream()
                .sorted(Comparator.comparing(Role::getId))
                .map(r -> new RoleSummaryDTO(r.getId(), r.getName(), r.getDescription()))
                .collect(Collectors.toList());
        dto.setRoles(roles);

        return dto;
    }
}
