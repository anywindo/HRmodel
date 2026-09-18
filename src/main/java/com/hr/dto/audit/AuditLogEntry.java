package com.hr.dto.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLogEntry {
    private String id; // UUID
    private LocalDateTime timestamp;
    private String username;
    private String method;
    private String uri;
    private String clientIp;
    private int status;
    private String previousHash; // Hash of the previous entry
    private String hash;         // Hash of this entry (including previousHash)
    
    @JsonProperty("contentForHashing")
    private String contentForHashing;
    
    @JsonIgnore
    private boolean valid = true;
    
    @JsonIgnore
    private String errorMsg;
    
    public AuditLogEntry() {}
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    
    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }
    
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    
    public void setContentForHashing(String contentForHashing) {
        this.contentForHashing = contentForHashing;
    }
    
    /**
     * String representation used for hashing this entry.
     * EXCLUDES the 'hash' field itself, as the hash is calculated from this content.
     */
    public String getContentForHashing() {
        if (contentForHashing != null && !contentForHashing.isEmpty()) {
            return contentForHashing;
        }
        return String.format("%s|%s|%s|%s|%s|%s|%d|%s",
                id != null ? id : "",
                timestamp != null ? timestamp.toString() : "",
                username != null ? username : "",
                method != null ? method : "",
                uri != null ? uri : "",
                clientIp != null ? clientIp : "",
                status,
                previousHash != null ? previousHash : "");
    }
}
