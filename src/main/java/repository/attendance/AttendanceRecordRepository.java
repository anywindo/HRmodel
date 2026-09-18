package repository.attendance;

import model.attendance.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByEmployeeIdOrderByDateDesc(Long employeeId);
    List<AttendanceRecord> findByEmployeeIdAndDateBetweenOrderByDateAsc(Long employeeId, LocalDate startDate, LocalDate endDate);
    Optional<AttendanceRecord> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    List<AttendanceRecord> findByDate(LocalDate date);
}
