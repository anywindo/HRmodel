package com.hr.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Time;
import java.time.LocalTime;

@Converter(autoApply = true)
public class LocalTimeConverter implements AttributeConverter<LocalTime, Time> {

    @Override
    public Time convertToDatabaseColumn(LocalTime attribute) {
        return attribute == null ? null : Time.valueOf(attribute);
    }

    @Override
    public LocalTime convertToEntityAttribute(Time dbData) {
        if (dbData == null) {
            return null;
        }
        // Avoid java.sql.Time.toLocalTime() which has a JDK bug under certain timezones (e.g. Asia/Jakarta)
        // causing negative nanoseconds (e.g. -276000000) when calculating epoch millis.
        return LocalTime.parse(dbData.toString());
    }
}
