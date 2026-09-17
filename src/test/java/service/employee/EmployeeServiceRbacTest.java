package service.employee;

import com.hr.dto.EmployeeResponse;
import model.employee.*;
import model.department.Department;
import model.department.DepartmentId;
import model.position.Position;
import model.position.PositionId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import repository.employee.EmployeeRepository;
import repository.leave.LeaveDelegationRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceRbacTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private LeaveDelegationRepository leaveDelegationRepository;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee1; // Department A, Manager
    private Employee employee2; // Department A, Report to employee1
    private Employee employee3; // Department B, Independent

    @BeforeEach
    void setUp() throws Exception {
        SecurityContextHolder.setContext(securityContext);

        // Setup mock employees via reflection
        java.lang.reflect.Constructor<Employee> empConstructor = Employee.class.getDeclaredConstructor();
        empConstructor.setAccessible(true);
        employee1 = empConstructor.newInstance();
        employee2 = empConstructor.newInstance();
        employee3 = empConstructor.newInstance();

        java.lang.reflect.Constructor<Position> posConstructor = Position.class.getDeclaredConstructor();
        posConstructor.setAccessible(true);
        Position pos1 = posConstructor.newInstance();
        Position pos2 = posConstructor.newInstance();
        Position pos3 = posConstructor.newInstance();

        java.lang.reflect.Constructor<Department> deptConstructor = Department.class.getDeclaredConstructor();
        deptConstructor.setAccessible(true);
        Department deptA = deptConstructor.newInstance();
        Department deptB = deptConstructor.newInstance();

        ReflectionTestUtils.setField(deptA, "departmentId", new DepartmentId("DEPT-A"));
        ReflectionTestUtils.setField(deptB, "departmentId", new DepartmentId("DEPT-B"));

        ReflectionTestUtils.setField(pos1, "positionId", new PositionId("POS-1"));
        ReflectionTestUtils.setField(pos1, "department", deptA);

        ReflectionTestUtils.setField(pos2, "positionId", new PositionId("POS-2"));
        ReflectionTestUtils.setField(pos2, "department", deptA);
        ReflectionTestUtils.setField(pos2, "reportsTo", pos1); // pos2 reports to pos1

        ReflectionTestUtils.setField(pos3, "positionId", new PositionId("POS-3"));
        ReflectionTestUtils.setField(pos3, "department", deptB);

        ReflectionTestUtils.setField(employee1, "id", 1L);
        ReflectionTestUtils.setField(employee1, "employeeId", "EMP-1");
        ReflectionTestUtils.setField(employee1, "email", new Email("emp1@test.com"));
        ReflectionTestUtils.setField(employee1, "fullName", new FullName("Emp", "", "One"));
        ReflectionTestUtils.setField(employee1, "phoneNumber", new PhoneNumber("1234567890", "+1"));
        ReflectionTestUtils.setField(employee1, "salary", new Salary(new java.math.BigDecimal("50000")));
        ReflectionTestUtils.setField(employee1, "dateOfBirth", LocalDate.of(1990, 1, 1));
        ReflectionTestUtils.setField(employee1, "hireDate", LocalDate.of(2020, 1, 1));
        ReflectionTestUtils.setField(employee1, "gender", Gender.MAN);
        ReflectionTestUtils.setField(employee1, "sex", Sex.MALE);
        ReflectionTestUtils.setField(employee1, "maritalStatus", MaritalStatus.SINGLE);
        ReflectionTestUtils.setField(employee1, "position", pos1);
        ReflectionTestUtils.setField(employee1, "status", EmployeeStatus.ACTIVE);

        ReflectionTestUtils.setField(employee2, "id", 2L);
        ReflectionTestUtils.setField(employee2, "employeeId", "EMP-2");
        ReflectionTestUtils.setField(employee2, "email", new Email("emp2@test.com"));
        ReflectionTestUtils.setField(employee2, "fullName", new FullName("Emp", "", "Two"));
        ReflectionTestUtils.setField(employee2, "phoneNumber", new PhoneNumber("2234567890", "+1"));
        ReflectionTestUtils.setField(employee2, "salary", new Salary(new java.math.BigDecimal("60000")));
        ReflectionTestUtils.setField(employee2, "dateOfBirth", LocalDate.of(1990, 1, 1));
        ReflectionTestUtils.setField(employee2, "hireDate", LocalDate.of(2020, 1, 1));
        ReflectionTestUtils.setField(employee2, "gender", Gender.WOMAN);
        ReflectionTestUtils.setField(employee2, "sex", Sex.FEMALE);
        ReflectionTestUtils.setField(employee2, "maritalStatus", MaritalStatus.MARRIED);
        ReflectionTestUtils.setField(employee2, "position", pos2);
        ReflectionTestUtils.setField(employee2, "status", EmployeeStatus.ACTIVE);

        ReflectionTestUtils.setField(employee3, "id", 3L);
        ReflectionTestUtils.setField(employee3, "employeeId", "EMP-3");
        ReflectionTestUtils.setField(employee3, "email", new Email("emp3@test.com"));
        ReflectionTestUtils.setField(employee3, "fullName", new FullName("Emp", "", "Three"));
        ReflectionTestUtils.setField(employee3, "phoneNumber", new PhoneNumber("3234567890", "+1"));
        ReflectionTestUtils.setField(employee3, "salary", new Salary(new java.math.BigDecimal("70000")));
        ReflectionTestUtils.setField(employee3, "dateOfBirth", LocalDate.of(1990, 1, 1));
        ReflectionTestUtils.setField(employee3, "hireDate", LocalDate.of(2020, 1, 1));
        ReflectionTestUtils.setField(employee3, "gender", Gender.MAN);
        ReflectionTestUtils.setField(employee3, "sex", Sex.MALE);
        ReflectionTestUtils.setField(employee3, "maritalStatus", MaritalStatus.SINGLE);
        ReflectionTestUtils.setField(employee3, "position", pos3);
        ReflectionTestUtils.setField(employee3, "status", EmployeeStatus.ACTIVE);

        when(employeeRepository.findAll()).thenReturn(List.of(employee1, employee2, employee3));
        when(leaveDelegationRepository.findAllActiveDelegations(any(LocalDate.class))).thenReturn(Collections.emptyList());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetAllEmployees_AsSuperAdmin() {
        // Mock authentication for Super Admin
        when(securityContext.getAuthentication()).thenReturn(authentication);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        doReturn(authorities).when(authentication).getAuthorities();

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        // Super admin should see all 3 employees
        assertEquals(3, result.size());
    }

    @Test
    void testGetAllEmployees_AsHR() {
        // Mock authentication for HR
        when(securityContext.getAuthentication()).thenReturn(authentication);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_HR"));
        doReturn(authorities).when(authentication).getAuthorities();

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        // HR should see all 3 employees
        assertEquals(3, result.size());
    }

    @Test
    void testGetAllEmployees_AsExecutive() {
        // Mock authentication for EXECUTIVE
        when(securityContext.getAuthentication()).thenReturn(authentication);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_EXECUTIVE"));
        doReturn(authorities).when(authentication).getAuthorities();

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        // EXECUTIVE should see all 3 employees
        assertEquals(3, result.size());
    }

    @Test
    void testGetAllEmployees_AsStandardUser_WithReports() {
        // Mock authentication for Employee 1 (Standard User, Manager of Emp 2, in Dept A)
        when(securityContext.getAuthentication()).thenReturn(authentication);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(authentication).getAuthorities();
        when(authentication.isAuthenticated()).thenReturn(true);
        
        UserDetails userDetails = User.withUsername("emp1@test.com").password("").authorities("ROLE_USER").build();
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(employeeRepository.findByEmail_Value("emp1@test.com")).thenReturn(Optional.of(employee1));

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        // Employee 1 should see themselves and Employee 2 (same department and direct report)
        // Employee 3 is in Dept B and doesn't report to Emp 1, so shouldn't be visible.
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getEmployeeId().equals("EMP-1")));
        assertTrue(result.stream().anyMatch(e -> e.getEmployeeId().equals("EMP-2")));
    }

    @Test
    void testGetAllEmployees_Unauthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        // Unauthenticated should see 0 employees
        assertTrue(result.isEmpty());
    }
}
