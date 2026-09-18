package com.hr.dto.audit;

import java.util.List;

public class AuditLogResponse {
    private List<AuditLogEntryDTO> data;
    private int totalPages;
    private long totalElements;
    private boolean tampered;

    public AuditLogResponse() {}

    public List<AuditLogEntryDTO> getData() {
        return data;
    }

    public void setData(List<AuditLogEntryDTO> data) {
        this.data = data;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public boolean isTampered() {
        return tampered;
    }

    public void setTampered(boolean tampered) {
        this.tampered = tampered;
    }

    // A separate DTO so we can expose the 'valid' and 'errorMsg' fields to the frontend
    // without polluting the file serialization.
    public static class AuditLogEntryDTO extends AuditLogEntry {
        // Overriding the JsonIgnore from the superclass just for the response
        @com.fasterxml.jackson.annotation.JsonProperty
        @Override
        public boolean isValid() {
            return super.isValid();
        }

        @com.fasterxml.jackson.annotation.JsonProperty
        @Override
        public String getErrorMsg() {
            return super.getErrorMsg();
        }
        
        public static AuditLogEntryDTO from(AuditLogEntry entry) {
            AuditLogEntryDTO dto = new AuditLogEntryDTO();
            dto.setId(entry.getId());
            dto.setTimestamp(entry.getTimestamp());
            dto.setUsername(entry.getUsername());
            dto.setMethod(entry.getMethod());
            dto.setUri(entry.getUri());
            dto.setClientIp(entry.getClientIp());
            dto.setStatus(entry.getStatus());
            dto.setPreviousHash(entry.getPreviousHash());
            dto.setHash(entry.getHash());
            dto.setValid(entry.isValid());
            dto.setErrorMsg(entry.getErrorMsg());
            return dto;
        }
    }
}
