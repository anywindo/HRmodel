package service.employee;

import com.hr.dto.EmployeeRequest;
import com.hr.dto.EmployeeResponse;
import model.employee.*;
import model.position.Position;
import model.position.PositionId;
import model.auth.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import repository.employee.EmployeeRepository;
import repository.position.PositionRepository;
import repository.auth.RoleRepository;
import repository.leave.EmployeeLeaveBalanceRepository;
import repository.leave.LeaveDelegationRepository;
import service.notification.NotificationService;
import service.email.EmailService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PositionRepository positionRepository;
    @Mock
    private EmployeeLeaveBalanceRepository balanceRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private LeaveDelegationRepository leaveDelegationRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee mockEmployee;
    private Position mockPosition;

    @BeforeEach
    void setUp() {
        try {
            java.lang.reflect.Constructor<Employee> constructor = Employee.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            mockEmployee = constructor.newInstance();
            
            java.lang.reflect.Constructor<Position> posConstructor = Position.class.getDeclaredConstructor();
            posConstructor.setAccessible(true);
            mockPosition = posConstructor.newInstance();
            ReflectionTestUtils.setField(mockPosition, "positionId", new PositionId("POS-001"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate mocks via reflection", e);
        }

        ReflectionTestUtils.setField(mockEmployee, "employeeId", "EMP-001");
        ReflectionTestUtils.setField(mockEmployee, "fullName", new FullName("Jane", "", "Doe"));
        ReflectionTestUtils.setField(mockEmployee, "email", new Email("jane.doe@example.com"));
        ReflectionTestUtils.setField(mockEmployee, "phoneNumber", new PhoneNumber("1234567890", "+1"));
        ReflectionTestUtils.setField(mockEmployee, "salary", new Salary(new BigDecimal("50000")));
        ReflectionTestUtils.setField(mockEmployee, "status", EmployeeStatus.ACTIVE);
        ReflectionTestUtils.setField(mockEmployee, "dateOfBirth", LocalDate.of(1990, 1, 1));
        ReflectionTestUtils.setField(mockEmployee, "hireDate", LocalDate.now());
        ReflectionTestUtils.setField(mockEmployee, "gender", Gender.MAN);
        ReflectionTestUtils.setField(mockEmployee, "sex", Sex.MALE);
        ReflectionTestUtils.setField(mockEmployee, "maritalStatus", MaritalStatus.SINGLE);
        ReflectionTestUtils.setField(mockEmployee, "position", mockPosition);
    }

    @Test
    void createEmployee_Success() {
        EmployeeRequest request = new EmployeeRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setEmail("john.smith@example.com");
        request.setPhoneNumber("1234567890");
        request.setPhoneCountryCode("+1");
        request.setSalaryAmount(new BigDecimal("50000"));
        request.setGender("MAN");
        request.setSex("MALE");
        request.setMaritalStatus("SINGLE");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setHireDate(LocalDate.now());

        Role role = new Role();
        role.setName("ROLE_USER");
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        Employee createdEmployee = mockEmployee; // reuse mock for simplicity in save return
        when(employeeRepository.save(any(Employee.class))).thenReturn(createdEmployee);

        EmployeeResponse response = employeeService.createEmployee(request);

        assertNotNull(response);
        verify(employeeRepository).save(any(Employee.class));
        verify(balanceRepository).save(any());
        verify(notificationService, atLeastOnce()).notifyRole(eq("HR"), anyString(), any(), anyString(), anyString());
    }

    @Test
    void updateEmployee_Success() {
        EmployeeRequest request = new EmployeeRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe-Smith"); // Name change
        
        when(employeeRepository.findByEmployeeId("EMP-001")).thenReturn(Optional.of(mockEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(mockEmployee);

        EmployeeResponse response = employeeService.updateEmployee("EMP-001", request);

        assertNotNull(response);
        verify(employeeRepository).save(mockEmployee);
        verify(notificationService).createNotification(eq("EMP-001"), anyString(), any(), anyString(), anyString());
    }

    @Test
    void changeEmployeeStatus_Success() {
        SecurityContextHolder.setContext(securityContext);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser"); // Different user performing the action

        when(employeeRepository.findByEmployeeId("EMP-001")).thenReturn(Optional.of(mockEmployee));

        employeeService.changeEmployeeStatus("EMP-001", "TERMINATED", "Policy violation");

        verify(employeeRepository).save(mockEmployee);
        assertEquals(EmployeeStatus.TERMINATED, mockEmployee.getStatus());
        verify(notificationService).notifyRole(eq("HR"), anyString(), any(), eq("EMP-001"), anyString());
        verify(emailService).sendEmail(eq("jane.doe@example.com"), eq("Notice of Employment Termination"), anyString());
        
        SecurityContextHolder.clearContext();
    }

    @Test
    void changeEmployeeStatus_FailsWithoutReason() {
        when(employeeRepository.findByEmployeeId("EMP-001")).thenReturn(Optional.of(mockEmployee));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            employeeService.changeEmployeeStatus("EMP-001", "SUSPENDED", "   ")
        );

        assertEquals("A reason is required for status changes", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }
}
