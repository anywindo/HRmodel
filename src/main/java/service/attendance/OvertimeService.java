package service.attendance;

import com.hr.dto.attendance.OvertimeRequestDTO;
import com.hr.dto.attendance.OvertimeResponseDTO;
import model.attendance.AttendanceRecord;
import model.attendance.OvertimeRequest;
import model.attendance.OvertimeRequestStatus;
import model.employee.Employee;
import model.notification.NotificationType;
import model.position.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.attendance.AttendanceRecordRepository;
import repository.attendance.OvertimeRequestRepository;
import repository.employee.EmployeeRepository;
import repository.position.PositionRepository;
import service.notification.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OvertimeService {

    @Autowired
    private OvertimeRequestRepository overtimeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PositionRepository positionRepository;

    @Transactional
    public OvertimeResponseDTO submitRequest(String employeeId, OvertimeRequestDTO dto) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
                
        OvertimeRequest request = new OvertimeRequest(employee, dto.getDate(), dto.getHoursRequested(), dto.getReason());
        
        request = overtimeRepository.save(request);
        
        // Find manager by reportsTo
        if (employee.getPosition() != null && employee.getPosition().getReportsTo() != null) {
            String managerPositionId = employee.getPosition().getReportsTo().getPositionId().getValue();
            List<Employee> managers = employeeRepository.findByPosition_PositionId_Value(managerPositionId);
            if (!managers.isEmpty()) {
                notificationService.createNotification(
                        managers.get(0).getEmployeeId(),
                        employee.getFullName().getFirstName() + " has requested " + dto.getHoursRequested() + " hours of overtime for " + dto.getDate(),
                        NotificationType.OVERTIME_REQUEST,
                        request.getId().toString()
                );
            }
        }
        
        return mapToDTO(request);
    }

    public List<OvertimeResponseDTO> getMyRequests(String employeeId) {
        return overtimeRepository.findByEmployee_EmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<OvertimeResponseDTO> getPendingForManager(String managerId) {
        Employee myEmp = employeeRepository.findByEmployeeId(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
                
        if (myEmp.getPosition() == null) return List.of();
        
        List<String> subPosIds = getAllSubordinatePositionIds(myEmp.getPosition().getPositionId().getValue());
        
        if (subPosIds.isEmpty()) return List.of();
                
        return overtimeRepository.findByEmployee_Position_PositionId_ValueInAndStatusOrderByCreatedAtDesc(subPosIds, OvertimeRequestStatus.PENDING_MANAGER)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    
    public List<OvertimeResponseDTO> getPendingForHR() {
        return overtimeRepository.findByStatusInOrderByCreatedAtDesc(List.of(OvertimeRequestStatus.PENDING_HR, OvertimeRequestStatus.PENDING_MANAGER))
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public OvertimeResponseDTO approveByManager(Long requestId, String managerId) {
        OvertimeRequest request = getRequest(requestId);
        
        request.setStatus(OvertimeRequestStatus.PENDING_HR);
        request = overtimeRepository.save(request);
        
        return mapToDTO(request);
    }

    @Transactional
    public OvertimeResponseDTO approveByHR(Long requestId, String hrId) {
        OvertimeRequest request = getRequest(requestId);
        
        if (request.getStatus() != OvertimeRequestStatus.PENDING_HR && request.getStatus() != OvertimeRequestStatus.PENDING_MANAGER) {
            throw new RuntimeException("Request is not pending approval");
        }
        
        request.setStatus(OvertimeRequestStatus.APPROVED);
        request = overtimeRepository.save(request);

        
        // Update AttendanceRecord if it exists for that date
        java.util.Optional<AttendanceRecord> optRecord = attendanceRecordRepository.findByEmployeeIdAndDate(
                request.getEmployee().getId(), request.getDate());
                
        if (optRecord.isPresent()) {
            AttendanceRecord record = optRecord.get();
            record.setOvertimeHours(request.getHoursRequested());
            attendanceRecordRepository.save(record);
            request.setAttendanceRecord(record);
            overtimeRepository.save(request);
        }
        
        notificationService.createNotification(
                request.getEmployee().getEmployeeId(),
                "Your overtime request for " + request.getDate() + " has been approved.",
                NotificationType.OVERTIME_APPROVED,
                request.getId().toString()
        );
        
        return mapToDTO(request);
    }

    @Transactional
    public OvertimeResponseDTO rejectRequest(Long requestId, String rejectedById) {
        OvertimeRequest request = getRequest(requestId);
        request.setStatus(OvertimeRequestStatus.REJECTED);
        request = overtimeRepository.save(request);
        
        notificationService.createNotification(
                request.getEmployee().getEmployeeId(),
                "Your overtime request for " + request.getDate() + " has been rejected.",
                NotificationType.OVERTIME_REJECTED,
                request.getId().toString()
        );
        
        return mapToDTO(request);
    }

    private OvertimeRequest getRequest(Long id) {
        return overtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
    }

    private OvertimeResponseDTO mapToDTO(OvertimeRequest request) {
        OvertimeResponseDTO dto = new OvertimeResponseDTO();
        dto.setId(request.getId());
        dto.setEmployeeId(request.getEmployee().getEmployeeId());
        dto.setEmployeeName(request.getEmployee().getFullName().getFirstName() + " " + request.getEmployee().getFullName().getLastName());
        dto.setDate(request.getDate());
        dto.setHoursRequested(request.getHoursRequested());
        dto.setReason(request.getReason());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        return dto;
    }
    
    private List<String> getAllSubordinatePositionIds(String rootPositionId) {
        List<Position> allPositions = positionRepository.findAll();
        List<String> subordinates = new ArrayList<>();
        findSubordinatesRecursive(rootPositionId, allPositions, subordinates);
        return subordinates;
    }
    
    private void findSubordinatesRecursive(String managerPositionId, List<Position> allPositions, List<String> subordinates) {
        for (Position p : allPositions) {
            if (p.getReportsTo() != null && p.getReportsTo().getPositionId().getValue().equals(managerPositionId)) {
                subordinates.add(p.getPositionId().getValue());
                findSubordinatesRecursive(p.getPositionId().getValue(), allPositions, subordinates);
            }
        }
    }
}
