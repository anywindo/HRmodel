package com.hr.controller.leave;

import com.hr.dto.leave.LeaveSettingsDTO;
import com.hr.dto.leave.PublicHolidayDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.leave.LeaveSettingsService;

import java.util.List;

@RestController
@RequestMapping("/api/leave/settings")
public class LeaveSettingsController {

    private final LeaveSettingsService leaveSettingsService;

    public LeaveSettingsController(LeaveSettingsService leaveSettingsService) {
        this.leaveSettingsService = leaveSettingsService;
    }

    @GetMapping
    public ResponseEntity<LeaveSettingsDTO> getSettings() {
        return ResponseEntity.ok(leaveSettingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<LeaveSettingsDTO> updateSettings(@RequestBody LeaveSettingsDTO dto) {
        return ResponseEntity.ok(leaveSettingsService.updateSettings(dto));
    }

    @GetMapping("/holidays")
    public ResponseEntity<List<PublicHolidayDTO>> getAllHolidays() {
        return ResponseEntity.ok(leaveSettingsService.getAllHolidays());
    }

    @PostMapping("/holidays")
    public ResponseEntity<PublicHolidayDTO> addHoliday(@RequestBody PublicHolidayDTO dto) {
        return ResponseEntity.ok(leaveSettingsService.addHoliday(dto));
    }

    @DeleteMapping("/holidays/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        leaveSettingsService.deleteHoliday(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/holidays/import")
    public ResponseEntity<Integer> importHolidays(@RequestBody java.util.Map<String, Object> payload) {
        int year = (Integer) payload.get("year");
        String countryCode = (String) payload.get("countryCode");
        int count = leaveSettingsService.importHolidays(year, countryCode);
        return ResponseEntity.ok(count);
    }
}
