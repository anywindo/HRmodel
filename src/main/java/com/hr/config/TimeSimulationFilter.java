package com.hr.config;

import com.hr.utils.TimeUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class TimeSimulationFilter extends OncePerRequestFilter {

    private static final String SIMULATED_TIME_HEADER = "X-Simulated-Date";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String simulatedTimeStr = request.getHeader(SIMULATED_TIME_HEADER);
        if (simulatedTimeStr != null && !simulatedTimeStr.isBlank()) {
            try {
                LocalDateTime simulatedTime = LocalDateTime.parse(simulatedTimeStr, DateTimeFormatter.ISO_DATE_TIME);
                TimeUtil.setSimulatedTime(simulatedTime);
            } catch (DateTimeParseException e) {
                // Ignore invalid formats or log them
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TimeUtil.clear();
        }
    }
}
