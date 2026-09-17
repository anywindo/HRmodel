package service.notification;

import model.employee.Employee;
import model.notification.Notification;
import model.notification.NotificationType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.employee.EmployeeRepository;
import repository.notification.NotificationRepository;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    public NotificationService(NotificationRepository notificationRepository, EmployeeRepository employeeRepository) {
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
    }

    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return employeeRepository.findByEmail_Value(userDetails.getUsername()).orElse(null);
    }

    @Transactional
    public void createNotification(String recipientEmployeeId, String message, NotificationType type, String referenceId) {
        createNotification(recipientEmployeeId, message, type, referenceId, null);
    }

    @Transactional
    public void createNotification(String recipientEmployeeId, String message, NotificationType type, String referenceId, String actionUrl) {
        Notification notification = new Notification(recipientEmployeeId, message, type, referenceId, actionUrl);
        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyRole(String roleName, String message, NotificationType type, String referenceId, String actionUrl) {
        List<Employee> employees = employeeRepository.findByRoles_Name(roleName);
        for (Employee emp : employees) {
            createNotification(emp.getEmployeeId(), message, type, referenceId, actionUrl);
        }
    }

    @Transactional
    public void notifyAllEmployees(String message, NotificationType type, String referenceId, String actionUrl) {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee emp : employees) {
            if (emp.getStatus() != model.employee.EmployeeStatus.TERMINATED) {
                createNotification(emp.getEmployeeId(), message, type, referenceId, actionUrl);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications() {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null) return List.of();
        return notificationRepository.findTop50ByRecipientEmployeeIdOrderByCreatedAtDesc(myEmp.getEmployeeId());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null) return 0;
        return notificationRepository.countByRecipientEmployeeIdAndIsReadFalse(myEmp.getEmployeeId());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null) return;
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getRecipientEmployeeId().equals(myEmp.getEmployeeId())) {
            throw new IllegalArgumentException("Cannot mark another user's notification as read");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        Employee myEmp = getCurrentEmployee();
        if (myEmp == null) return;
        List<Notification> unread = notificationRepository.findByRecipientEmployeeIdAndIsReadFalse(myEmp.getEmployeeId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
