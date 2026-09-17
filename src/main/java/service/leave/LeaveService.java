package service.leave;

import com.hr.dto.leave.LeaveBalanceDTO;
import com.hr.dto.leave.LeaveRequestDTO;
import com.hr.dto.leave.LeaveResponseDTO;
import model.auth.User;
import model.employee.Employee;
import model.leave.EmployeeLeaveBalance;
import model.leave.LeaveRequest;
import model.leave.LeaveStatus;
import model.leave.LeaveType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.auth.UserRepository;
import repository.leave.EmployeeLeaveBalanceRepository;
import repository.leave.LeaveRequestRepository;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeLeaveBalanceRepository balanceRepository;
    private final UserRepository userRepository;

    public LeaveService(LeaveRequestRepository leaveRequestRepository, EmployeeLeaveBalanceRepository balanceRepository, UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.balanceRepository = balanceRepository;
        this.userRepository = userRepository;
    }
    
    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        return currentUser != null ? currentUser.getEmployee() : null;
    }

    @Transactional
    public LeaveBalanceDTO getMyBalance() {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null) throw new IllegalArgumentException("User has no linked employee profile");
        
        EmployeeLeaveBalance balance = balanceRepository.findByEmployee_EmployeeId(myEmp.getEmployeeId())
            .orElseGet(() -> {
                EmployeeLeaveBalance newBalance = new EmployeeLeaveBalance(myEmp, 12, 14);
                return balanceRepository.save(newBalance);
            });
            
        return new LeaveBalanceDTO(balance);
    }
    
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getMyRequests() {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null) return List.of();
        
        return leaveRequestRepository.findByEmployee_EmployeeIdOrderByCreatedAtDesc(myEmp.getEmployeeId())
                .stream().map(LeaveResponseDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public LeaveResponseDTO submitRequest(LeaveRequestDTO dto) {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null) throw new IllegalArgumentException("User has no linked employee profile");
        
        LeaveType type = LeaveType.valueOf(dto.getLeaveType().toUpperCase());
        long daysRequested = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        if (daysRequested <= 0) throw new IllegalArgumentException("End date must be on or after start date");
        
        EmployeeLeaveBalance balance = balanceRepository.findByEmployee_EmployeeId(myEmp.getEmployeeId())
            .orElseGet(() -> {
                EmployeeLeaveBalance newBalance = new EmployeeLeaveBalance(myEmp, 12, 14);
                return balanceRepository.save(newBalance);
            });
            
        if (type == LeaveType.ANNUAL && balance.getAnnualLeaveDays() < daysRequested) {
            throw new IllegalArgumentException("Insufficient annual leave balance");
        } else if (type == LeaveType.SICK && balance.getSickLeaveDays() < daysRequested) {
            throw new IllegalArgumentException("Insufficient sick leave balance");
        }
        
        LeaveRequest request = new LeaveRequest(myEmp, type, dto.getStartDate(), dto.getEndDate(), dto.getReason());
        return new LeaveResponseDTO(leaveRequestRepository.save(request));
    }
    
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getPendingManagerApprovals() {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null || myEmp.getPosition() == null) return List.of();
        
        // Find requests where status = PENDING_MANAGER and the employee reports to this manager
        return leaveRequestRepository.findByStatusAndEmployee_Position_ReportsTo_PositionId_Value(
                LeaveStatus.PENDING_MANAGER, myEmp.getPosition().getPositionId().getValue())
                .stream().map(LeaveResponseDTO::new).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getPendingHRApprovals() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING_HR)
                .stream().map(LeaveResponseDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public LeaveResponseDTO approveRequestManager(String requestId) {
        Employee myEmp = getCurrentEmployee();
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
            
        if (request.getStatus() != LeaveStatus.PENDING_MANAGER) {
            throw new IllegalArgumentException("Request is not waiting for manager approval");
        }
        
        request.setStatus(LeaveStatus.PENDING_HR);
        request.setManagerApprover(myEmp);
        return new LeaveResponseDTO(leaveRequestRepository.save(request));
    }

    @Transactional
    public LeaveResponseDTO approveRequestHR(String requestId) {
        Employee hrEmp = getCurrentEmployee();
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
            
        if (request.getStatus() != LeaveStatus.PENDING_HR) {
            throw new IllegalArgumentException("Request is not waiting for HR approval");
        }
        
        request.setStatus(LeaveStatus.APPROVED);
        request.setHrApprover(hrEmp);
        
        // Deduct balance
        long daysRequested = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        EmployeeLeaveBalance balance = balanceRepository.findByEmployee_EmployeeId(request.getEmployee().getEmployeeId())
            .orElseThrow(() -> new IllegalArgumentException("Balance not found"));
            
        if (request.getLeaveType() == LeaveType.ANNUAL) {
            balance.setAnnualLeaveDays((int)(balance.getAnnualLeaveDays() - daysRequested));
        } else if (request.getLeaveType() == LeaveType.SICK) {
            balance.setSickLeaveDays((int)(balance.getSickLeaveDays() - daysRequested));
        }
        
        balanceRepository.save(balance);
        return new LeaveResponseDTO(leaveRequestRepository.save(request));
    }
    
    @Transactional
    public LeaveResponseDTO rejectRequest(String requestId, boolean isManager) {
        Employee approver = getCurrentEmployee();
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
            
        request.setStatus(LeaveStatus.REJECTED);
        if (isManager) {
            request.setManagerApprover(approver);
        } else {
            request.setHrApprover(approver);
        }
        return new LeaveResponseDTO(leaveRequestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getApprovalsForMe() {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null) return List.of();
        
        List<LeaveResponseDTO> approvals = new ArrayList<>();
        
        if (myEmp.getPosition() != null) {
            approvals.addAll(leaveRequestRepository.findByStatusAndEmployee_Position_ReportsTo_PositionId_Value(
                    LeaveStatus.PENDING_MANAGER, myEmp.getPosition().getPositionId().getValue())
                    .stream().map(LeaveResponseDTO::new).collect(Collectors.toList()));
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> 
            a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("SUPER_ADMIN") || a.getAuthority().equals("HR"))) {
            
            approvals.addAll(leaveRequestRepository.findByStatus(LeaveStatus.PENDING_HR)
                    .stream().map(LeaveResponseDTO::new).collect(Collectors.toList()));
        }
        
        return approvals;
    }

    @Transactional
    public LeaveResponseDTO approveRequest(String requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
            
        if (request.getStatus() == LeaveStatus.PENDING_MANAGER) {
            return approveRequestManager(requestId);
        } else if (request.getStatus() == LeaveStatus.PENDING_HR) {
            return approveRequestHR(requestId);
        } else {
            throw new IllegalArgumentException("Request cannot be approved in its current state");
        }
    }

    @Transactional
    public LeaveResponseDTO rejectRequest(String requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
            
        if (request.getStatus() == LeaveStatus.PENDING_MANAGER) {
            return rejectRequest(requestId, true);
        } else if (request.getStatus() == LeaveStatus.PENDING_HR) {
            return rejectRequest(requestId, false);
        } else {
            throw new IllegalArgumentException("Request cannot be rejected in its current state");
        }
    }

    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getApprovalHistory() {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null) return List.of();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isHrOrAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a ->
            a.getAuthority().equals("ROLE_HR") || a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("SUPER_ADMIN") || a.getAuthority().equals("HR"));

        List<LeaveStatus> doneStatuses = java.util.Arrays.asList(
            LeaveStatus.APPROVED, LeaveStatus.REJECTED, LeaveStatus.PENDING_HR
        );

        if (isHrOrAdmin) {
            // HR and Suparadmin see all completed requests
            return leaveRequestRepository.findByStatusInOrderByCreatedAtDesc(
                    java.util.Arrays.asList(LeaveStatus.APPROVED, LeaveStatus.REJECTED))
                    .stream().map(LeaveResponseDTO::new).collect(Collectors.toList());
        }

        // Managers see requests they have acted on (moved to PENDING_HR, APPROVED, or REJECTED)
        if (myEmp.getPosition() != null) {
            return leaveRequestRepository.findByManagerApprover_EmployeeId(myEmp.getEmployeeId())
                    .stream().map(LeaveResponseDTO::new).collect(Collectors.toList());
        }

        return List.of();
    }
}
