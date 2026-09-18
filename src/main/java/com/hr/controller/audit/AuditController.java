package com.hr.controller.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hr.dto.audit.AuditLogEntry;
import com.hr.dto.audit.AuditLogResponse;
import com.hr.dto.audit.AuditLogResponse.AuditLogEntryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import service.audit.AuditLoggerService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final ObjectMapper objectMapper;
    private final String LOG_DIR = "audit_logs";

    public AuditController() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('role:view')")
    public ResponseEntity<AuditLogResponse> getLogs(
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        LocalDate targetDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();
        String filename = LOG_DIR + "/audit-" + targetDate.toString() + ".jsonl";
        File file = new File(filename);

        List<AuditLogEntry> allEntries = new ArrayList<>();
        boolean tampered = false;

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                String expectedPreviousHash = "";
                int lineNumber = 1;
                
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    
                    try {
                        AuditLogEntry entry = objectMapper.readValue(line, AuditLogEntry.class);
                        
                        // Hash Verification
                        // 1. Verify previous hash link
                        if (lineNumber > 1 && !expectedPreviousHash.equals(entry.getPreviousHash())) {
                            entry.setValid(false);
                            entry.setErrorMsg("Chain broken: previousHash mismatch at line " + lineNumber);
                            tampered = true;
                        }
                        
                        // 2. Verify current hash
                        String recalculatedHash = AuditLoggerService.calculateHash(entry.getContentForHashing());
                        if (!recalculatedHash.equals(entry.getHash())) {
                            System.out.println("HASH MISMATCH at line " + lineNumber);
                            System.out.println("Expected hash: " + entry.getHash());
                            System.out.println("Recalc hash:   " + recalculatedHash);
                            System.out.println("Original content used for hash calculation according to log:");
                            System.out.println(line);
                            System.out.println("Re-evaluated content:");
                            System.out.println(entry.getContentForHashing());
                            entry.setValid(false);
                            entry.setErrorMsg("Content tampered: hash mismatch at line " + lineNumber);
                            tampered = true;
                        }
                        
                        allEntries.add(entry);
                        expectedPreviousHash = entry.getHash();
                        
                    } catch (Exception e) {
                        // Unparseable line
                        AuditLogEntry corruptEntry = new AuditLogEntry();
                        corruptEntry.setValid(false);
                        corruptEntry.setErrorMsg("Corrupt or unparseable JSON at line " + lineNumber);
                        allEntries.add(corruptEntry);
                        tampered = true;
                    }
                    lineNumber++;
                }
            } catch (Exception e) {
                // Return server error
                return ResponseEntity.internalServerError().build();
            }
        }

        // Pagination
        int totalElements = allEntries.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // Ensure page is within bounds
        int currentPage = Math.max(1, Math.min(page, Math.max(1, totalPages)));
        
        int fromIndex = (currentPage - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalElements);
        
        List<AuditLogEntryDTO> pagedData = new ArrayList<>();
        if (fromIndex < totalElements) {
            pagedData = allEntries.subList(fromIndex, toIndex).stream()
                    .map(AuditLogEntryDTO::from)
                    .collect(Collectors.toList());
        }

        AuditLogResponse response = new AuditLogResponse();
        response.setData(pagedData);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setTampered(tampered);

        return ResponseEntity.ok(response);
    }
}
