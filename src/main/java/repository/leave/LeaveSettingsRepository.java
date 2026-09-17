package repository.leave;

import model.leave.LeaveSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveSettingsRepository extends JpaRepository<LeaveSettings, Long> {
}
