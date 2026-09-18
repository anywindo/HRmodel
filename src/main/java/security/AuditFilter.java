package security;

import com.hr.dto.audit.AuditLogEntry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
    private final JwtUtil jwtUtil;

    public AuditFilter(AuditLoggerService auditLoggerService, JwtUtil jwtUtil) {
        this.auditLoggerService = auditLoggerService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Exclude internal automated pings, static assets, and system health monitors
        if (uri.startsWith("/assets") ||
            uri.startsWith("/uploads") ||
            uri.equals("/favicon.ico") ||
            uri.startsWith("/instances") ||
            uri.startsWith("/actuator") ||
            uri.equals("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            String username = null;

            // 1. Try SecurityContextHolder
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() &&
                !auth.getName().equalsIgnoreCase("anonymousUser") &&
                !auth.getName().equalsIgnoreCase("anonymous")) {
                username = auth.getName();
            }

            // 2. Fallback: extract username directly from auth_token cookie
            if (username == null && request.getCookies() != null && jwtUtil != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("auth_token".equals(cookie.getName())) {
                        try {
                            String email = jwtUtil.extractEmail(cookie.getValue());
                            if (email != null && !email.trim().isEmpty()) {
                                username = email;
                            }
                        } catch (Exception ignored) {}
                        break;
                    }
                }
            }

            // 3. Fallback: extract username from Authorization header
            if (username == null && jwtUtil != null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        String email = jwtUtil.extractEmail(authHeader.substring(7));
                        if (email != null && !email.trim().isEmpty()) {
                            username = email;
                        }
                    } catch (Exception ignored) {}
                }
            }

            if (username == null || username.trim().isEmpty()) {
                username = "anonymous";
            }

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
