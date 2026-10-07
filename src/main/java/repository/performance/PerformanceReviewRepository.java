package repository.performance;

import model.performance.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployeeId(Long employeeId);
    List<PerformanceReview> findByReviewerId(Long reviewerId);
}
