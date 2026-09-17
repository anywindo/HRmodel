package service.leave;

import com.hr.dto.leave.LeaveRequestDTO;
import com.hr.dto.leave.LeaveResponseDTO;
import model.employee.Email;
import model.employee.Employee;
import model.employee.EmployeeStatus;
import model.employee.FullName;
import model.leave.EmployeeLeaveBalance;
import model.leave.LeaveRequest;
import model.leave.LeaveSettings;
import model.leave.LeaveStatus;
import model.leave.LeaveType;
import model.position.Position;
import model.position.PositionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import repository.employee.EmployeeRepository;
import repository.leave.EmployeeLeaveBalanceRepository;
import repository.leave.LeaveDelegationRepository;
import repository.leave.LeaveRequestRepository;
import repository.leave.LeaveSettingsRepository;
import repository.leave.PublicHolidayRepository;
import repository.position.PositionRepository;
import service.notification.NotificationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private EmployeeLeaveBalanceRepository balanceRepository;
    @Mock
    private PublicHolidayRepository publicHolidayRepository;
    @Mock
    private LeaveSettingsRepository leaveSettingsRepository;
    @Mock
    private PositionRepository positionRepository;
    @Mock
    private LeaveDelegationRepository leaveDelegationRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private NotificationService notificationService;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private LeaveService leaveService;

    private Employee mockEmployee;
    private EmployeeLeaveBalance mockBalance;

    @BeforeEach
    void setUp() {
        // Setup mock employee
        try {
            java.lang.reflect.Constructor<Employee> constructor = Employee.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            mockEmployee = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate Employee", e);
        }
        org.springframework.test.util.ReflectionTestUtils.setField(mockEmployee, "employeeId", "EMP-001");
        try {
            java.lang.reflect.Constructor<Position> posConstructor = Position.class.getDeclaredConstructor();
            posConstructor.setAccessible(true);
            Position mockPosition = posConstructor.newInstance();
            Position mockManagerPosition = posConstructor.newInstance();
            org.springframework.test.util.ReflectionTestUtils.setField(mockPosition, "positionId", new PositionId("POS-001"));
            org.springframework.test.util.ReflectionTestUtils.setField(mockManagerPosition, "positionId", new PositionId("POS-MGR"));
            org.springframework.test.util.ReflectionTestUtils.setField(mockPosition, "reportsTo", mockManagerPosition);
            org.springframework.test.util.ReflectionTestUtils.setField(mockEmployee, "position", mockPosition);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate Position", e);
        }
        org.springframework.test.util.ReflectionTestUtils.setField(mockEmployee, "fullName", new FullName("John", "", "Doe"));
        org.springframework.test.util.ReflectionTestUtils.setField(mockEmployee, "email", new Email("john.doe@example.com"));
        org.springframework.test.util.ReflectionTestUtils.setField(mockEmployee, "status", EmployeeStatus.ACTIVE);

        // Setup mock balance
        mockBalance = new EmployeeLeaveBalance(mockEmployee, 12, 14);

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        UserDetails userDetails = User.withUsername("test@company.com").password("pass").roles("USER").build();
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(employeeRepository.findByEmail_Value("test@company.com")).thenReturn(Optional.of(mockEmployee));
    }

    @Test
    void submitRequest_Success() {
        setupAuthentication();

        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setLeaveType("ANNUAL");
        dto.setStartDate(LocalDate.now().plusDays(1)); // Tomorrow
        // Calculate an end date that ensures at least 1 working day (skip weekends)
        LocalDate endDate = dto.getStartDate();
        while (endDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || endDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            endDate = endDate.plusDays(1);
        }
        dto.setEndDate(endDate);
        dto.setReason("Vacation");

        when(publicHolidayRepository.findByDateBetween(any(), any())).thenReturn(new ArrayList<>());
        when(balanceRepository.findByEmployee_EmployeeId(mockEmployee.getEmployeeId())).thenReturn(Optional.of(mockBalance));
        when(leaveRequestRepository.findByEmployee_EmployeeId(mockEmployee.getEmployeeId())).thenReturn(new ArrayList<>());
        
        LeaveRequest savedRequest = new LeaveRequest(mockEmployee, LeaveType.ANNUAL, dto.getStartDate(), dto.getEndDate(), dto.getReason());
        savedRequest.setRequestId("REQ-001");
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(savedRequest);

        LeaveResponseDTO response = leaveService.submitRequest(dto);

        assertNotNull(response);
        assertEquals("REQ-001", response.getRequestId());
        assertEquals(LeaveStatus.PENDING_MANAGER.name(), response.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void submitRequest_InsufficientBalance() {
        setupAuthentication();

        mockBalance.setAnnualLeaveDays(0); // No days left

        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setLeaveType("ANNUAL");
        dto.setStartDate(LocalDate.now().plusDays(1));
        LocalDate endDate = dto.getStartDate();
        while (endDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || endDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            endDate = endDate.plusDays(1);
        }
        dto.setEndDate(endDate);
        dto.setReason("Vacation");

        when(publicHolidayRepository.findByDateBetween(any(), any())).thenReturn(new ArrayList<>());
        when(balanceRepository.findByEmployee_EmployeeId(mockEmployee.getEmployeeId())).thenReturn(Optional.of(mockBalance));
        when(leaveRequestRepository.findByEmployee_EmployeeId(mockEmployee.getEmployeeId())).thenReturn(new ArrayList<>());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            leaveService.submitRequest(dto);
        });
        assertTrue(ex.getMessage().contains("Insufficient annual leave balance"));
    }

    @Test
    void approveRequestManager_Success() {
        setupAuthentication();

        LeaveRequest request = new LeaveRequest(mockEmployee, LeaveType.ANNUAL, LocalDate.now(), LocalDate.now().plusDays(1), "Vacation");
        request.setRequestId("REQ-001");
        request.setStatus(LeaveStatus.PENDING_MANAGER);

        when(leaveRequestRepository.findById("REQ-001")).thenReturn(Optional.of(request));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(request);

        LeaveResponseDTO response = leaveService.approveRequestManager("REQ-001");

        assertNotNull(response);
        assertEquals(LeaveStatus.PENDING_HR.name(), response.getStatus());
        verify(notificationService, times(1)).createNotification(anyString(), anyString(), any(), anyString(), anyString());
    }

    @Test
    void cancelRequest_RefundsBalance() {
        setupAuthentication();

        LeaveRequest request = new LeaveRequest(mockEmployee, LeaveType.ANNUAL, LocalDate.now().plusDays(10), LocalDate.now().plusDays(11), "Vacation");
        request.setRequestId("REQ-001");
        request.setStatus(LeaveStatus.APPROVED);

        when(leaveRequestRepository.findById("REQ-001")).thenReturn(Optional.of(request));
        when(publicHolidayRepository.findByDateBetween(any(), any())).thenReturn(new ArrayList<>());
        when(balanceRepository.findByEmployee_EmployeeId(mockEmployee.getEmployeeId())).thenReturn(Optional.of(mockBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(request);
        
        // Ensure the days calculation works
        double initialBalance = mockBalance.getAnnualLeaveDays();

        leaveService.cancelRequest("REQ-001");

        // The balance should be increased (refunded)
        assertTrue(mockBalance.getAnnualLeaveDays() > initialBalance);
        verify(balanceRepository, times(1)).save(mockBalance);
        assertEquals(LeaveStatus.CANCELLED, request.getStatus());
    }

    @Test
    void runMonthlyAccruals_Success() {
        LeaveSettings settings = new LeaveSettings();
        settings.setEnableAccrual(true);
        settings.setAccrualRatePerMonth(new BigDecimal("1.5"));

        when(leaveSettingsRepository.findAll()).thenReturn(List.of(settings));
        when(balanceRepository.findAll()).thenReturn(List.of(mockBalance));

        double initialAnnual = mockBalance.getAnnualLeaveDays();

        leaveService.runMonthlyAccruals();

        assertEquals(initialAnnual + 1.5, mockBalance.getAnnualLeaveDays());
        verify(balanceRepository, times(1)).save(mockBalance);
    }
}
