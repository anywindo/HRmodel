package com.hr.controller.position;

import com.hr.dto.PositionRequest;
import com.hr.dto.PositionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.position.PositionService;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@CrossOrigin(origins = "*") // For development, allow all origins
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR', 'EXECUTIVE', 'FINANCE') or hasAuthority('position:view')")
    public ResponseEntity<List<PositionResponse>> getAllPositions() {
        return ResponseEntity.ok(positionService.getAllPositions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR', 'EXECUTIVE', 'FINANCE') or hasAuthority('position:view')")
    public ResponseEntity<PositionResponse> getPositionById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(positionService.getPositionById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR') or hasAuthority('position:create')")
    public ResponseEntity<?> createPosition(@RequestBody PositionRequest request) {
        try {
            PositionResponse response = positionService.createPosition(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR') or hasAuthority('position:edit')")
    public ResponseEntity<?> updatePosition(@PathVariable String id, @RequestBody PositionRequest request) {
        try {
            PositionResponse response = positionService.updatePosition(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR') or hasAuthority('position:delete')")
    public ResponseEntity<?> deletePosition(@PathVariable String id) {
        try {
            positionService.deletePosition(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }
}
