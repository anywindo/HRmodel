package repository.payroll;

import model.payroll.TaxBracket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxBracketRepository extends JpaRepository<TaxBracket, Long> {
    List<TaxBracket> findBySettingsIdOrderByMinAmountAsc(Long settingsId);
    void deleteBySettingsId(Long settingsId);
}
