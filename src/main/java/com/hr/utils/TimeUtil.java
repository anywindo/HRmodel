package com.hr.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimeUtil {
    private static final ThreadLocal<LocalDateTime> simulatedTime = new ThreadLocal<>();

    public static void setSimulatedTime(LocalDateTime time) {
        simulatedTime.set(time);
    }

    public static void clear() {
        simulatedTime.remove();
    }

    public static LocalDateTime getLocalDateTimeNow() {
        LocalDateTime time = simulatedTime.get();
        if (time != null) {
            return time;
        }
        return LocalDateTime.now();
    }

    public static LocalDate getLocalDateNow() {
        LocalDateTime time = simulatedTime.get();
        if (time != null) {
            return time.toLocalDate();
        }
        return LocalDate.now();
    }
}
