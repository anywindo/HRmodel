package security;

import model.auth.Permission;
import model.auth.Role;
import model.auth.SystemAdmin;
import model.employee.Email;
import model.employee.Employee;
import model.employee.EmployeeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;
import repository.auth.SystemAdminRepository;
import repository.employee.EmployeeRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    
    @Mock
    private SystemAdminRepository systemAdminRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Employee mockEmployee;
    private SystemAdmin mockAdmin;
    private Role mockRole;

    @BeforeEach
    void setUp() throws Exception {
        mockRole = new Role();
        mockRole.setName("USER");
        Permission permission = new Permission();
        permission.setName("read:basic");
        mockRole.getPermissions().add(permission);

        java.lang.reflect.Constructor<Employee> empConstructor = Employee.class.getDeclaredConstructor();
        empConstructor.setAccessible(true);
        mockEmployee = empConstructor.newInstance();
        ReflectionTestUtils.setField(mockEmployee, "email", new Email("employee@test.com"));
        ReflectionTestUtils.setField(mockEmployee, "passwordHash", "hashedPass123");
        ReflectionTestUtils.setField(mockEmployee, "status", EmployeeStatus.ACTIVE);
        mockEmployee.getRoles().add(mockRole);

        mockAdmin = new SystemAdmin("admin@test.com", "adminPass123", new java.util.HashSet<>());
        Role adminRole = new Role();
        adminRole.setName("SUPER_ADMIN");
        mockAdmin.getRoles().add(adminRole);
    }

    @Test
    void loadUserByUsername_SystemAdmin_Success() {
        when(systemAdminRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(mockAdmin));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@test.com");

        assertNotNull(userDetails);
        assertEquals("admin@test.com", userDetails.getUsername());
        assertEquals("adminPass123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")));
    }

    @Test
    void loadUserByUsername_Employee_Success() {
        when(systemAdminRepository.findByEmail("employee@test.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail_Value("employee@test.com")).thenReturn(Optional.of(mockEmployee));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("employee@test.com");

        assertNotNull(userDetails);
        assertEquals("employee@test.com", userDetails.getUsername());
        assertEquals("hashedPass123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("read:basic")));
    }

    @Test
    void loadUserByUsername_EmployeeTerminated_ThrowsDisabledException() {
        ReflectionTestUtils.setField(mockEmployee, "status", EmployeeStatus.TERMINATED);
        when(systemAdminRepository.findByEmail("employee@test.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail_Value("employee@test.com")).thenReturn(Optional.of(mockEmployee));

        DisabledException exception = assertThrows(DisabledException.class, () -> {
            customUserDetailsService.loadUserByUsername("employee@test.com");
        });

        assertEquals("Employee account has been terminated", exception.getMessage());
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsUsernameNotFoundException() {
        when(systemAdminRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail_Value("unknown@test.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("unknown@test.com");
        });

        assertTrue(exception.getMessage().contains("User not found with email"));
    }
}
