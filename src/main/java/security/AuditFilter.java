package security;

import com.hr.dto.audit.AuditLogEntry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import service.audit.AuditLoggerService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public class AuditFilter extends OncePerRequestFilter {

    private final AuditLoggerService auditLoggerService;

    public AuditFilter(AuditLoggerService auditLoggerService) {
        this.auditLoggerService = auditLoggerService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip static resources or common exclusions if needed
        String uri = request.getRequestURI();
        if (uri.startsWith("/assets") || uri.startsWith("/uploads") || uri.equals("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // After request completes (even if it threw an error)
            String username = "anonymous";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                username = auth.getName();
            }

            // In some cases (like login error) authentication might be null, but we still log
            
            // Do not log the audit endpoint itself to avoid infinite loops of reading/logging
            // Actually it's fine to log it, but it might create noise. We'll log it anyway for completeness.

            AuditLogEntry entry = new AuditLogEntry();
            entry.setId(UUID.randomUUID().toString());
            entry.setTimestamp(LocalDateTime.now());
            entry.setMethod(request.getMethod());
            entry.setUri(uri);
            entry.setClientIp(getClientIp(request));
            entry.setStatus(response.getStatus());
            entry.setUsername(username);

            auditLoggerService.logEvent(entry);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
