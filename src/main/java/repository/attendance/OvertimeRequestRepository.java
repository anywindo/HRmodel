package repository.attendance;

import model.attendance.OvertimeRequest;
import model.attendance.OvertimeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {
    List<OvertimeRequest> findByEmployee_EmployeeIdOrderByCreatedAtDesc(String employeeId);
    List<OvertimeRequest> findByEmployee_Position_PositionId_ValueInAndStatusOrderByCreatedAtDesc(List<String> positionIds, OvertimeRequestStatus status);
    List<OvertimeRequest> findByStatusOrderByCreatedAtDesc(OvertimeRequestStatus status);
    List<OvertimeRequest> findByStatusInOrderByCreatedAtDesc(List<OvertimeRequestStatus> statuses);
}

