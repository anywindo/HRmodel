package repository.payroll;

import model.payroll.PayrollSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollSettingsRepository extends JpaRepository<PayrollSettings, Long> {
}
