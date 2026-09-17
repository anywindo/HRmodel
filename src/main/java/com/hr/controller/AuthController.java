package com.hr.controller;

import com.hr.dto.LoginRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import repository.employee.EmployeeRepository;
import model.employee.Employee;
import repository.position.PositionRepository;
import repository.auth.SystemAdminRepository;
import model.auth.SystemAdmin;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final security.JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final service.notification.NotificationService notificationService;
    private final SystemAdminRepository systemAdminRepository;

    public AuthController(AuthenticationManager authenticationManager, security.JwtUtil jwtUtil, PasswordEncoder passwordEncoder, EmployeeRepository employeeRepository, PositionRepository positionRepository, service.notification.NotificationService notificationService, SystemAdminRepository systemAdminRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.employeeRepository = employeeRepository;
        this.positionRepository = positionRepository;
        this.notificationService = notificationService;
        this.systemAdminRepository = systemAdminRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        String email = loginRequest.getEmail() != null ? loginRequest.getEmail().trim() : "";
        String password = loginRequest.getPassword() != null ? loginRequest.getPassword().trim() : "";
        
        System.out.println("Login attempt: email='" + email + "', password='" + password + "'");
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String token = jwtUtil.generateToken(userDetails.getUsername());

            // Create HTTP-Only cookie
            Cookie cookie = new Cookie("auth_token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400); // 1 day
            response.addCookie(cookie);

            List<String> permissions = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            Map<String, Object> responseBody = new HashMap<>();

            Optional<SystemAdmin> adminOpt = systemAdminRepository.findByEmail(userDetails.getUsername());
            if (adminOpt.isPresent()) {
                SystemAdmin admin = adminOpt.get();
                responseBody.put("userId", admin.getId());
                responseBody.put("email", admin.getEmail());
                responseBody.put("permissions", permissions);
                responseBody.put("name", "System Admin");
                responseBody.put("hasSubordinates", false);
                return ResponseEntity.ok(responseBody);
            }

            Employee employee = employeeRepository.findByEmail_Value(userDetails.getUsername()).orElseThrow();
            
            responseBody.put("userId", employee.getId());
            responseBody.put("email", employee.getEmail().getValue());
            responseBody.put("permissions", permissions);
            responseBody.put("name", employee.getFullName().getFirstName() + " " + employee.getFullName().getLastName());
            boolean hasSubordinates = false;
            if (employee.getPosition() != null) {
                String myPositionId = employee.getPosition().getPositionId().getValue();
                hasSubordinates = positionRepository.existsByReportsTo_PositionId_Value(myPositionId);
            }
            responseBody.put("hasSubordinates", hasSubordinates);
            responseBody.put("requiresPasswordChange", employee.isRequiresPasswordChange());

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        Map<String, Object> responseBody = new HashMap<>();

        Optional<SystemAdmin> adminOpt = systemAdminRepository.findByEmail(userDetails.getUsername());
        if (adminOpt.isPresent()) {
            SystemAdmin admin = adminOpt.get();
            responseBody.put("userId", admin.getId());
            responseBody.put("email", admin.getEmail());
            responseBody.put("permissions", permissions);
            responseBody.put("name", "System Admin");
            responseBody.put("hasSubordinates", false);
            return ResponseEntity.ok(responseBody);
        }

        Employee employee = employeeRepository.findByEmail_Value(userDetails.getUsername()).orElseThrow();
        
        responseBody.put("userId", employee.getId());
        responseBody.put("email", employee.getEmail().getValue());
        responseBody.put("permissions", permissions);
        responseBody.put("name", employee.getFullName().getFirstName() + " " + employee.getFullName().getLastName());
        
        boolean hasSubordinates = false;
        if (employee.getPosition() != null) {
            String myPositionId = employee.getPosition().getPositionId().getValue();
            hasSubordinates = positionRepository.existsByReportsTo_PositionId_Value(myPositionId);
        }
        responseBody.put("hasSubordinates", hasSubordinates);
        responseBody.put("requiresPasswordChange", employee.isRequiresPasswordChange());

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("auth_token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("Invalid password provided.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<SystemAdmin> adminOpt = systemAdminRepository.findByEmail(userDetails.getUsername());

        if (adminOpt.isPresent()) {
            SystemAdmin admin = adminOpt.get();
            if (!passwordEncoder.matches(currentPassword, admin.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect.");
            }
            admin.setPasswordHash(passwordEncoder.encode(newPassword));
            systemAdminRepository.save(admin);
            return ResponseEntity.ok().build();
        }

        Employee employee = employeeRepository.findByEmail_Value(userDetails.getUsername()).orElseThrow();

        if (!passwordEncoder.matches(currentPassword, employee.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect.");
        }

        employee.setPasswordHash(passwordEncoder.encode(newPassword));
        employee.setRequiresPasswordChange(false);
        Employee savedEmployee = employeeRepository.save(employee);

        notificationService.createNotification(
            savedEmployee.getEmployeeId(),
            "Your account security credentials (password) have been updated.",
            model.notification.NotificationType.GENERAL,
            String.valueOf(savedEmployee.getId()),
            "/profile"
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activateAccount() {
        return ResponseEntity.badRequest().body("Activation flow is currently disabled.");
    }
}
