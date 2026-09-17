package com.hr.controller.rbac;

import com.hr.dto.rbac.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import service.rbac.RbacService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rbac")
public class RbacController {

    private final RbacService rbacService;

    public RbacController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    // --- PERMISSIONS ---
    @GetMapping("/permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('role:view')")
    public ResponseEntity<List<PermissionDTO>> getPermissions() {
        return ResponseEntity.ok(rbacService.getAllPermissions());
    }

    // --- ROLES ---
    @GetMapping("/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('role:view')")
    public ResponseEntity<List<RoleResponseDTO>> getRoles() {
        return ResponseEntity.ok(rbacService.getAllRoles());
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('role:view')")
    public ResponseEntity<RoleResponseDTO> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(rbacService.getRoleById(id));
    }

    @PostMapping("/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('role:create')")
    public ResponseEntity<?> createRole(@RequestBody RoleRequestDTO req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(rbacService.createRole(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('role:edit')")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody RoleRequestDTO req) {
        try {
            return ResponseEntity.ok(rbacService.updateRole(id, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('role:delete')")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            rbacService.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // --- USERS ---
    @GetMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('user:view')")
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        return ResponseEntity.ok(rbacService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('user:view')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(rbacService.getUserById(id));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('user:create')")
    public ResponseEntity<?> createUser(@RequestBody UserRequestDTO req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(rbacService.createUser(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('user:edit')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRequestDTO req) {
        try {
            return ResponseEntity.ok(rbacService.updateUser(id, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('user:delete')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            rbacService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
