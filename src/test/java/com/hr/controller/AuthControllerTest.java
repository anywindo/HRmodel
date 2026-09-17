package com.hr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hr.dto.LoginRequest;
import model.employee.Email;
import model.employee.Employee;
import model.employee.FullName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import repository.auth.SystemAdminRepository;
import repository.employee.EmployeeRepository;
import repository.position.PositionRepository;
import security.JwtUtil;
import service.notification.NotificationService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PositionRepository positionRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private SystemAdminRepository systemAdminRepository;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = User.withUsername("test@example.com").password("password").authorities("ROLE_USER").build();
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("mock-jwt-token");
        
        when(systemAdminRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        
        java.lang.reflect.Constructor<Employee> empConstructor = Employee.class.getDeclaredConstructor();
        empConstructor.setAccessible(true);
        Employee mockEmployee = empConstructor.newInstance();
        ReflectionTestUtils.setField(mockEmployee, "id", 1L);
        ReflectionTestUtils.setField(mockEmployee, "email", new Email("test@example.com"));
        ReflectionTestUtils.setField(mockEmployee, "fullName", new FullName("John", "", "Doe"));
        when(employeeRepository.findByEmail_Value("test@example.com")).thenReturn(Optional.of(mockEmployee));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("auth_token"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_Authenticated() throws Exception {
        UserDetails userDetails = User.withUsername("test@example.com").password("").authorities("ROLE_USER").build();

        java.lang.reflect.Constructor<Employee> empConstructor = Employee.class.getDeclaredConstructor();
        empConstructor.setAccessible(true);
        Employee mockEmployee = empConstructor.newInstance();
        ReflectionTestUtils.setField(mockEmployee, "id", 1L);
        ReflectionTestUtils.setField(mockEmployee, "email", new Email("test@example.com"));
        ReflectionTestUtils.setField(mockEmployee, "fullName", new FullName("John", "", "Doe"));
        
        when(systemAdminRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail_Value("test@example.com")).thenReturn(Optional.of(mockEmployee));

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);
        
        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        try {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("test@example.com"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void getMe_Unauthenticated() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
