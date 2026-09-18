package service.attendance;

import com.hr.dto.attendance.AttendanceRecordDTO;
import model.attendance.AttendanceRecord;
import model.attendance.AttendanceStatus;
import model.employee.Employee;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.attendance.AttendanceRecordRepository;
import repository.employee.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceService(AttendanceRecordRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<AttendanceRecordDTO> getMyAttendance(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records;
        if (startDate != null && endDate != null) {
            records = attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, startDate, endDate);
        } else {
            records = attendanceRepository.findByEmployeeIdOrderByDateDesc(employeeId);
        }
        return records.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<AttendanceRecordDTO> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public AttendanceRecordDTO checkIn(Long employeeId, LocalTime time) {
        time = time.withNano(0);
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseGet(() -> {
                    Employee emp = employeeRepository.findById(employeeId)
                            .orElseThrow(() -> new RuntimeException("Employee not found"));
                    return new AttendanceRecord(emp, today, AttendanceStatus.PRESENT);
                });
                
        // If already checked in, don't overwrite unless explicitly requested (simple implementation: just set it)
        record.setCheckInTime(time);
        
        // Basic late logic (e.g., after 09:00 AM)
        if (time.isAfter(LocalTime.of(9, 0))) {
            record.setStatus(AttendanceStatus.LATE);
        } else {
            record.setStatus(AttendanceStatus.PRESENT);
        }
        
        return mapToDTO(attendanceRepository.save(record));
    }

    public AttendanceRecordDTO checkOut(Long employeeId, LocalTime time) {
        time = time.withNano(0);
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("No check-in record found for today"));
                
        record.setCheckOutTime(time);
        
        // Basic overtime calculation (e.g., after 18:00 PM)
        if (time.isAfter(LocalTime.of(18, 0))) {
            long minutesOvertime = java.time.Duration.between(LocalTime.of(18, 0), time).toMinutes();
            if (minutesOvertime > 0) {
                record.setOvertimeHours(BigDecimal.valueOf(minutesOvertime).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP));
            }
        }
        
        return mapToDTO(attendanceRepository.save(record));
    }

    public AttendanceRecordDTO updateAttendance(Long id, AttendanceRecordDTO dto) {
        AttendanceRecord record = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
                
        record.setCheckInTime(dto.getCheckInTime());
        record.setCheckOutTime(dto.getCheckOutTime());
        record.setStatus(dto.getStatus());
        record.setOvertimeHours(dto.getOvertimeHours());
        record.setNotes(dto.getNotes());
        
        return mapToDTO(attendanceRepository.save(record));
    }
    
    // Admin / HR adding manual record
    public AttendanceRecordDTO addAttendance(AttendanceRecordDTO dto) {
        Employee emp = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
                
        AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDate(emp.getId(), dto.getDate())
                .orElse(new AttendanceRecord(emp, dto.getDate(), dto.getStatus()));
                
        record.setCheckInTime(dto.getCheckInTime());
        record.setCheckOutTime(dto.getCheckOutTime());
        record.setStatus(dto.getStatus());
        record.setOvertimeHours(dto.getOvertimeHours());
        record.setNotes(dto.getNotes());
        
        return mapToDTO(attendanceRepository.save(record));
    }

    private AttendanceRecordDTO mapToDTO(AttendanceRecord record) {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setId(record.getId());
        dto.setEmployeeId(record.getEmployee().getId());
        dto.setEmployeeName(record.getEmployee().getFullName().getFirstName() + " " + record.getEmployee().getFullName().getLastName());
        dto.setDate(record.getDate());
        dto.setCheckInTime(record.getCheckInTime());
        dto.setCheckOutTime(record.getCheckOutTime());
        dto.setStatus(record.getStatus());
        dto.setOvertimeHours(record.getOvertimeHours());
        dto.setNotes(record.getNotes());
        return dto;
    }
}
