package repository.notification;

import model.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop50ByRecipientEmployeeIdOrderByCreatedAtDesc(String recipientEmployeeId);

    long countByRecipientEmployeeIdAndIsReadFalse(String recipientEmployeeId);

    List<Notification> findByRecipientEmployeeIdAndIsReadFalse(String recipientEmployeeId);
}
