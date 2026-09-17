package service.leave;

import com.hr.dto.leave.LeaveBalanceDTO;
import com.hr.dto.leave.LeaveRequestDTO;
import com.hr.dto.leave.LeaveResponseDTO;

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

import repository.leave.EmployeeLeaveBalanceRepository;
import repository.leave.LeaveRequestRepository;
import repository.position.PositionRepository;
import model.position.Position;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import repository.leave.PublicHolidayRepository;
import repository.leave.LeaveSettingsRepository;
import model.leave.LeaveSettings;
import org.springframework.scheduling.annotation.Scheduled;
import model.leave.LeaveDelegation;
import repository.leave.LeaveDelegationRepository;
import com.hr.dto.leave.LeaveDelegationDTO;
import repository.employee.EmployeeRepository;
import service.notification.NotificationService;
import model.notification.NotificationType;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeLeaveBalanceRepository balanceRepository;
    private final PublicHolidayRepository publicHolidayRepository;
    private final LeaveSettingsRepository leaveSettingsRepository;
    private final PositionRepository positionRepository;
    private final LeaveDelegationRepository leaveDelegationRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public LeaveService(LeaveRequestRepository leaveRequestRepository, EmployeeLeaveBalanceRepository balanceRepository, PublicHolidayRepository publicHolidayRepository, LeaveSettingsRepository leaveSettingsRepository, PositionRepository positionRepository, LeaveDelegationRepository leaveDelegationRepository, EmployeeRepository employeeRepository, NotificationService notificationService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.balanceRepository = balanceRepository;
        this.publicHolidayRepository = publicHolidayRepository;
        this.leaveSettingsRepository = leaveSettingsRepository;
        this.positionRepository = positionRepository;
        this.leaveDelegationRepository = leaveDelegationRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }
    
    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return employeeRepository.findByEmail_Value(userDetails.getUsername()).orElse(null);
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
            
        // Calculate pending days to subtract from displayed balance
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findByEmployee_EmployeeId(myEmp.getEmployeeId()).stream()
            .filter(r -> r.getStatus() == LeaveStatus.PENDING_MANAGER || r.getStatus() == LeaveStatus.PENDING_HR)
            .collect(Collectors.toList());

        long pendingAnnual = 0;
        long pendingSick = 0;
        
        for (LeaveRequest r : pendingRequests) {
            long days = 0;
            List<LocalDate> holidays = publicHolidayRepository.findByDateBetween(r.getStartDate(), r.getEndDate())
                .stream().map(h -> h.getDate()).collect(Collectors.toList());
            for (LocalDate date = r.getStartDate(); !date.isAfter(r.getEndDate()); date = date.plusDays(1)) {
                if (date.getDayOfWeek() != java.time.DayOfWeek.SATURDAY && 
                    date.getDayOfWeek() != java.time.DayOfWeek.SUNDAY &&
                    !holidays.contains(date)) {
                    days++;
                }
            }
            if (r.getLeaveType() == LeaveType.ANNUAL) pendingAnnual += days;
            else if (r.getLeaveType() == LeaveType.SICK) pendingSick += days;
        }

        LeaveBalanceDTO dto = new LeaveBalanceDTO(balance);
        dto.setAnnualLeaveDays(balance.getAnnualLeaveDays() - pendingAnnual);
        dto.setSickLeaveDays(balance.getSickLeaveDays() - pendingSick);
        return dto;
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
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }
        
        long daysRequested = 0;
        List<LocalDate> holidays = publicHolidayRepository.findByDateBetween(dto.getStartDate(), dto.getEndDate())
            .stream().map(h -> h.getDate()).collect(Collectors.toList());

        for (LocalDate date = dto.getStartDate(); !date.isAfter(dto.getEndDate()); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != java.time.DayOfWeek.SATURDAY && 
                date.getDayOfWeek() != java.time.DayOfWeek.SUNDAY &&
                !holidays.contains(date)) {
                daysRequested++;
            }
        }
        
        if (daysRequested <= 0) throw new IllegalArgumentException("Requested period contains no working days");
        
        EmployeeLeaveBalance balance = balanceRepository.findByEmployee_EmployeeId(myEmp.getEmployeeId())
            .orElseGet(() -> {
                EmployeeLeaveBalance newBalance = new EmployeeLeaveBalance(myEmp, 12, 14);
                return balanceRepository.save(newBalance);
            });
            
        // Calculate already pending days for this leave type
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findByEmployee_EmployeeId(myEmp.getEmployeeId()).stream()
            .filter(r -> r.getStatus() == LeaveStatus.PENDING_MANAGER || r.getStatus() == LeaveStatus.PENDING_HR)
            .filter(r -> r.getLeaveType() == type)
            .collect(Collectors.toList());

        long pendingDays = 0;
        for (LeaveRequest r : pendingRequests) {
            for (LocalDate date = r.getStartDate(); !date.isAfter(r.getEndDate()); date = date.plusDays(1)) {
                if (date.getDayOfWeek() != java.time.DayOfWeek.SATURDAY && 
                    date.getDayOfWeek() != java.time.DayOfWeek.SUNDAY &&
                    !holidays.contains(date)) {
                    pendingDays++;
                }
            }
        }
            
        if (type == LeaveType.ANNUAL && (balance.getAnnualLeaveDays() - pendingDays) < daysRequested) {
            throw new IllegalArgumentException("Insufficient annual leave balance (including pending requests)");
        } else if (type == LeaveType.SICK && (balance.getSickLeaveDays() - pendingDays) < daysRequested) {
            throw new IllegalArgumentException("Insufficient sick leave balance (including pending requests)");
        }
        
        LeaveRequest request = new LeaveRequest(myEmp, type, dto.getStartDate(), dto.getEndDate(), dto.getReason());
        request.setAttachmentUrl(dto.getAttachmentUrl());
        LeaveResponseDTO response = new LeaveResponseDTO(leaveRequestRepository.save(request));
        
        // Notify manager(s)
        String empName = myEmp.getFullName().getFirstName() + " " + myEmp.getFullName().getLastName();
        String msg = empName + " submitted a " + type.name() + " leave request (" + dto.getStartDate() + " to " + dto.getEndDate() + ")";
        if (myEmp.getPosition() != null && myEmp.getPosition().getReportsTo() != null) {
            String mgrPosId = myEmp.getPosition().getReportsTo().getPositionId().getValue();
            employeeRepository.findByPosition_PositionId_Value(mgrPosId).forEach(mgr ->
                notificationService.createNotification(mgr.getEmployeeId(), msg, NotificationType.LEAVE_SUBMITTED, request.getRequestId(), "/pto/approvals")
            );
        }
        return response;
    }
    
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getPendingManagerApprovals() {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null || myEmp.getPosition() == null) return List.of();
        
        List<String> subPosIds = getAllSubordinatePositionIds(myEmp.getPosition().getPositionId().getValue());
        
        // Add subordinates of delegators who delegated to me
        List<LeaveDelegation> delegations = leaveDelegationRepository.findActiveDelegationsForDelegatee(myEmp.getEmployeeId(), LocalDate.now());
        for (LeaveDelegation del : delegations) {
            if (del.getDelegator().getPosition() != null) {
                subPosIds.addAll(getAllSubordinatePositionIds(del.getDelegator().getPosition().getPositionId().getValue()));
            }
        }
        
        if (subPosIds.isEmpty()) return List.of();
        
        // Find requests where status = PENDING_MANAGER and the employee reports to this manager recursively
        return leaveRequestRepository.findByStatusAndEmployee_Position_PositionId_ValueIn(
                LeaveStatus.PENDING_MANAGER, subPosIds)
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
        LeaveResponseDTO response = new LeaveResponseDTO(leaveRequestRepository.save(request));
        
        // Notify employee: approved by manager, pending HR
        notificationService.createNotification(
            request.getEmployee().getEmployeeId(),
            "Your " + request.getLeaveType().name() + " leave request was approved by your manager. Pending HR approval.",
            NotificationType.LEAVE_APPROVED_MANAGER,
            request.getRequestId(),
            "/pto/my-leave"
        );
        return response;
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
        long daysRequested = 0;
        List<LocalDate> holidays = publicHolidayRepository.findByDateBetween(request.getStartDate(), request.getEndDate())
            .stream().map(h -> h.getDate()).collect(Collectors.toList());

        for (LocalDate date = request.getStartDate(); !date.isAfter(request.getEndDate()); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != java.time.DayOfWeek.SATURDAY && 
                date.getDayOfWeek() != java.time.DayOfWeek.SUNDAY &&
                !holidays.contains(date)) {
                daysRequested++;
            }
        }
        EmployeeLeaveBalance balance = balanceRepository.findByEmployee_EmployeeId(request.getEmployee().getEmployeeId())
            .orElseThrow(() -> new IllegalArgumentException("Balance not found"));
            
        if (request.getLeaveType() == LeaveType.ANNUAL) {
            if (balance.getAnnualLeaveDays() < daysRequested) {
                throw new IllegalArgumentException("Insufficient annual leave balance at time of approval");
            }
            balance.setAnnualLeaveDays(balance.getAnnualLeaveDays() - daysRequested);
        } else if (request.getLeaveType() == LeaveType.SICK) {
            if (balance.getSickLeaveDays() < daysRequested) {
                throw new IllegalArgumentException("Insufficient sick leave balance at time of approval");
            }
            balance.setSickLeaveDays(balance.getSickLeaveDays() - daysRequested);
        }
        
        balanceRepository.save(balance);
        LeaveResponseDTO response = new LeaveResponseDTO(leaveRequestRepository.save(request));
        
        // Notify employee: fully approved
        notificationService.createNotification(
            request.getEmployee().getEmployeeId(),
            "Your " + request.getLeaveType().name() + " leave request (" + request.getStartDate() + " to " + request.getEndDate() + ") has been fully approved.",
            NotificationType.LEAVE_APPROVED_HR,
            request.getRequestId(),
            "/pto/my-leave"
        );
        return response;
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
        LeaveResponseDTO response = new LeaveResponseDTO(leaveRequestRepository.save(request));
        
        // Notify employee: rejected
        notificationService.createNotification(
            request.getEmployee().getEmployeeId(),
            "Your " + request.getLeaveType().name() + " leave request (" + request.getStartDate() + " to " + request.getEndDate() + ") was rejected.",
            NotificationType.LEAVE_REJECTED,
            request.getRequestId(),
            "/pto/my-leave"
        );
        return response;
    }

    @Transactional
    public LeaveResponseDTO cancelRequest(String requestId) {
        Employee myEmp = getCurrentEmployee();
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
            
        if (!request.getEmployee().getEmployeeId().equals(myEmp.getEmployeeId())) {
            throw new IllegalArgumentException("You can only cancel your own leave requests");
        }
        
        if (request.getStatus() == LeaveStatus.CANCELLED || request.getStatus() == LeaveStatus.REJECTED) {
            throw new IllegalArgumentException("Cannot cancel a request that is already " + request.getStatus());
        }
        
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot cancel a leave that has already started or is in the past. Please contact HR.");
        }
        
        // If it was already approved, we need to refund the balance
        if (request.getStatus() == LeaveStatus.APPROVED) {
            long daysRequested = 0;
            List<LocalDate> holidays = publicHolidayRepository.findByDateBetween(request.getStartDate(), request.getEndDate())
                .stream().map(h -> h.getDate()).collect(Collectors.toList());

            for (LocalDate date = request.getStartDate(); !date.isAfter(request.getEndDate()); date = date.plusDays(1)) {
                if (date.getDayOfWeek() != java.time.DayOfWeek.SATURDAY && 
                    date.getDayOfWeek() != java.time.DayOfWeek.SUNDAY &&
                    !holidays.contains(date)) {
                    daysRequested++;
                }
            }
            
            EmployeeLeaveBalance balance = balanceRepository.findByEmployee_EmployeeId(myEmp.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Balance not found"));
                
            if (request.getLeaveType() == LeaveType.ANNUAL) {
                balance.setAnnualLeaveDays(balance.getAnnualLeaveDays() + daysRequested);
            } else if (request.getLeaveType() == LeaveType.SICK) {
                balance.setSickLeaveDays(balance.getSickLeaveDays() + daysRequested);
            }
            balanceRepository.save(balance);
        }
        
        request.setStatus(LeaveStatus.CANCELLED);
        LeaveResponseDTO response = new LeaveResponseDTO(leaveRequestRepository.save(request));
        
        // Notify manager approver (if any) about the cancellation
        String empName = myEmp.getFullName().getFirstName() + " " + myEmp.getFullName().getLastName();
        if (request.getManagerApprover() != null) {
            notificationService.createNotification(
                request.getManagerApprover().getEmployeeId(),
                empName + " cancelled their " + request.getLeaveType().name() + " leave request (" + request.getStartDate() + " to " + request.getEndDate() + ").",
                NotificationType.LEAVE_CANCELLED,
                request.getRequestId(),
                "/pto/approvals"
            );
        }
        return response;
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

    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getApprovalsForMe() {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null) return List.of();
        
        List<LeaveResponseDTO> approvals = new ArrayList<>();
        
        if (myEmp.getPosition() != null) {
            List<String> subPosIds = getAllSubordinatePositionIds(myEmp.getPosition().getPositionId().getValue());
            
            // Add subordinates of delegators who delegated to me
            List<LeaveDelegation> delegations = leaveDelegationRepository.findActiveDelegationsForDelegatee(myEmp.getEmployeeId(), LocalDate.now());
            for (LeaveDelegation del : delegations) {
                if (del.getDelegator().getPosition() != null) {
                    subPosIds.addAll(getAllSubordinatePositionIds(del.getDelegator().getPosition().getPositionId().getValue()));
                }
            }
            
            if (!subPosIds.isEmpty()) {
                approvals.addAll(leaveRequestRepository.findByStatusAndEmployee_Position_PositionId_ValueIn(
                        LeaveStatus.PENDING_MANAGER, subPosIds)
                        .stream().map(LeaveResponseDTO::new).collect(Collectors.toList()));
            }
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

    @Scheduled(cron = "0 0 0 1 * *") // Run at midnight on the 1st of every month
    @Transactional
    public void runMonthlyAccruals() {
        LeaveSettings settings = leaveSettingsRepository.findAll().stream().findFirst().orElse(null);
        if (settings == null || !settings.isEnableAccrual()) {
            return;
        }

        List<EmployeeLeaveBalance> allBalances = balanceRepository.findAll();
        for (EmployeeLeaveBalance balance : allBalances) {
            if (balance.getEmployee().getStatus() == model.employee.EmployeeStatus.ACTIVE) {
                double currentAnnual = balance.getAnnualLeaveDays();
                double toAdd = settings.getAccrualRatePerMonth().doubleValue();
                balance.setAnnualLeaveDays(currentAnnual + toAdd);
                balanceRepository.save(balance);
            }
        }
    }

    @Scheduled(cron = "0 0 0 1 1 *") // Run at midnight on January 1st every year
    @Transactional
    public void runYearlyCarryOver() {
        LeaveSettings settings = leaveSettingsRepository.findAll().stream().findFirst().orElse(null);
        if (settings == null) {
            return;
        }

        List<EmployeeLeaveBalance> allBalances = balanceRepository.findAll();
        for (EmployeeLeaveBalance balance : allBalances) {
            if (balance.getEmployee().getStatus() == model.employee.EmployeeStatus.ACTIVE) {
                double currentAnnual = balance.getAnnualLeaveDays();
                double carryOver = Math.min(currentAnnual, settings.getMaxCarryOverDays());
                double startingBalance = settings.isEnableAccrual() ? 0 : settings.getDefaultAnnualLeaveDays();
                balance.setAnnualLeaveDays(carryOver + startingBalance);
                
                // Reset sick leave
                balance.setSickLeaveDays(settings.getDefaultSickLeaveDays());
                balanceRepository.save(balance);
            }
        }
    }

    @Transactional
    public LeaveDelegationDTO delegateApproval(LeaveDelegationDTO dto) {
        Employee delegator = getCurrentEmployee();
        if (delegator == null) throw new IllegalArgumentException("User has no linked employee profile");

        // Use findByEmployeeId because DTO passes employeeId which is a String, while findById takes Long id
        Employee delegatee = employeeRepository.findByEmployeeId(dto.getDelegateeId())
                .orElseThrow(() -> new IllegalArgumentException("Delegatee not found"));

        if (delegator.getEmployeeId().equals(delegatee.getEmployeeId())) {
            throw new IllegalArgumentException("Cannot delegate to yourself");
        }

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        LeaveDelegation delegation = new LeaveDelegation(delegator, delegatee, dto.getStartDate(), dto.getEndDate());
        LeaveDelegationDTO result = new LeaveDelegationDTO(leaveDelegationRepository.save(delegation));
        
        // Notify delegatee
        String delegatorName = delegator.getFullName().getFirstName() + " " + delegator.getFullName().getLastName();
        notificationService.createNotification(
            delegatee.getEmployeeId(),
            "You have been delegated approval authority by " + delegatorName + " (" + dto.getStartDate() + " to " + dto.getEndDate() + ").",
            NotificationType.DELEGATION_CREATED,
            delegation.getDelegationId(),
            "/pto/my-leave"
        );
        return result;
    }

    @Transactional(readOnly = true)
    public List<LeaveDelegationDTO> getMyDelegations() {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null) return List.of();

        return leaveDelegationRepository.findByDelegator_EmployeeIdOrderByStartDateDesc(myEmp.getEmployeeId())
                .stream().map(LeaveDelegationDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveDelegationDTO> getMyReceivedDelegations() {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null) return List.of();

        return leaveDelegationRepository.findByDelegatee_EmployeeIdOrderByStartDateDesc(myEmp.getEmployeeId())
                .stream().map(LeaveDelegationDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public LeaveDelegationDTO revokeDelegation(String id) {
        Employee myEmp = getCurrentEmployee();
        LeaveDelegation delegation = leaveDelegationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Delegation not found"));

        if (!delegation.getDelegator().getEmployeeId().equals(myEmp.getEmployeeId())) {
            throw new IllegalArgumentException("Can only revoke your own delegations");
        }

        delegation.setActive(false);
        LeaveDelegationDTO result = new LeaveDelegationDTO(leaveDelegationRepository.save(delegation));
        
        // Notify delegatee about revocation
        String delegatorName = delegation.getDelegator().getFullName().getFirstName() + " " + delegation.getDelegator().getFullName().getLastName();
        notificationService.createNotification(
            delegation.getDelegatee().getEmployeeId(),
            "Your delegated approval authority from " + delegatorName + " has been revoked.",
            NotificationType.DELEGATION_REVOKED,
            delegation.getDelegationId(),
            "/pto/my-leave"
        );
        return result;
    }

    public List<com.hr.dto.leave.LeaveCalendarEventDTO> getCalendarEvents(LocalDate start, LocalDate end) {
        List<com.hr.dto.leave.LeaveCalendarEventDTO> events = new ArrayList<>();

        // 1. Approved leave requests overlapping the range
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedOverlapping(start, end);
        for (LeaveRequest lr : approvedLeaves) {
            String empName = lr.getEmployee().getFullName().getFirstName() + " " + lr.getEmployee().getFullName().getLastName();
            String deptName = null;
            if (lr.getEmployee().getPosition() != null
                    && lr.getEmployee().getPosition().getDepartment() != null) {
                deptName = lr.getEmployee().getPosition().getDepartment().getName();
            }
            events.add(com.hr.dto.leave.LeaveCalendarEventDTO.fromLeaveRequest(
                    lr.getRequestId(),
                    empName,
                    lr.getLeaveType().name(),
                    lr.getStartDate(),
                    lr.getEndDate(),
                    lr.getStatus().name(),
                    lr.getEmployee().getEmployeeId(),
                    deptName
            ));
        }

        // 2. Public holidays in the range
        List<model.leave.PublicHoliday> holidays = publicHolidayRepository.findByDateBetween(start, end);
        for (model.leave.PublicHoliday ph : holidays) {
            events.add(com.hr.dto.leave.LeaveCalendarEventDTO.fromHoliday(ph.getId(), ph.getName(), ph.getDate()));
        }

        return events;
    }
}
