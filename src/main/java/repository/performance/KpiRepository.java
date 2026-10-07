package repository.performance;

import model.performance.Kpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KpiRepository extends JpaRepository<Kpi, Long> {
    List<Kpi> findByEmployeeId(Long employeeId);
}
