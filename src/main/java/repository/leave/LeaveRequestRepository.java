package repository.leave;

import model.leave.LeaveRequest;
import model.leave.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, String> {
    List<LeaveRequest> findByEmployee_EmployeeIdOrderByCreatedAtDesc(String employeeId);
    List<LeaveRequest> findByEmployee_EmployeeId(String employeeId);
    
    // For a manager to see pending requests from their direct subordinates
    List<LeaveRequest> findByStatusAndEmployee_Position_ReportsTo_PositionId_Value(LeaveStatus status, String managerPositionId);
    
    // For a manager to see pending requests from ANY subordinate in their branch
    List<LeaveRequest> findByStatusAndEmployee_Position_PositionId_ValueIn(LeaveStatus status, List<String> subordinatePositionIds);
    
    // For HR to see pending requests
    List<LeaveRequest> findByStatus(LeaveStatus status);

    // History: requests acted on by a manager (approved or rejected at manager stage)
    @Query("SELECT r FROM LeaveRequest r WHERE r.managerApprover.employeeId = :managerId ORDER BY r.createdAt DESC")
    List<LeaveRequest> findByManagerApprover_EmployeeId(@Param("managerId") String managerId);

    // History: all non-pending requests (for HR view)
    List<LeaveRequest> findByStatusInOrderByCreatedAtDesc(List<LeaveStatus> statuses);

    // Calendar: approved leaves overlapping a date range
    @Query("SELECT r FROM LeaveRequest r WHERE r.status = 'APPROVED' AND r.startDate <= :end AND r.endDate >= :start")
    List<LeaveRequest> findApprovedOverlapping(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);
}
