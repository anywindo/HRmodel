package service.performance;

import com.hr.dto.performance.KpiDTO;
import com.hr.dto.performance.PerformanceReviewDTO;
import model.employee.Employee;
import model.performance.Kpi;
import model.performance.PerformanceReview;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.employee.EmployeeRepository;
import repository.performance.KpiRepository;
import repository.performance.PerformanceReviewRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    private final KpiRepository kpiRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeeRepository employeeRepository;

    public PerformanceService(KpiRepository kpiRepository, PerformanceReviewRepository performanceReviewRepository, EmployeeRepository employeeRepository) {
        this.kpiRepository = kpiRepository;
        this.performanceReviewRepository = performanceReviewRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<KpiDTO> getKpisByEmployee(Long employeeId) {
        return kpiRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapKpiToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public KpiDTO createKpi(KpiDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId()).orElseThrow();
        Kpi kpi = new Kpi();
        kpi.setEmployee(employee);
        kpi.setTitle(dto.getTitle());
        kpi.setDescription(dto.getDescription());
        kpi.setTargetValue(dto.getTargetValue());
        kpi.setActualValue(dto.getActualValue() != null ? dto.getActualValue() : 0.0);
        kpi.setStatus(dto.getStatus());
        kpi.setStartDate(dto.getStartDate());
        kpi.setEndDate(dto.getEndDate());
        kpi.setWeight(dto.getWeight());
        
        Kpi saved = kpiRepository.save(kpi);
        return mapKpiToDTO(saved);
    }

    @Transactional
    public KpiDTO updateKpi(Long id, KpiDTO dto) {
        Kpi kpi = kpiRepository.findById(id).orElseThrow();
        kpi.setTitle(dto.getTitle());
        kpi.setDescription(dto.getDescription());
        kpi.setTargetValue(dto.getTargetValue());
        kpi.setActualValue(dto.getActualValue());
        kpi.setStatus(dto.getStatus());
        kpi.setStartDate(dto.getStartDate());
        kpi.setEndDate(dto.getEndDate());
        kpi.setWeight(dto.getWeight());
        
        Kpi saved = kpiRepository.save(kpi);
        return mapKpiToDTO(saved);
    }

    public void deleteKpi(Long id) {
        kpiRepository.deleteById(id);
    }

    public List<PerformanceReviewDTO> getReviewsByEmployee(Long employeeId) {
        return performanceReviewRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapReviewToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PerformanceReviewDTO createReview(PerformanceReviewDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId()).orElseThrow();
        Employee reviewer = dto.getReviewerId() != null ? employeeRepository.findById(dto.getReviewerId()).orElse(null) : null;
        
        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setReviewer(reviewer);
        review.setReviewPeriod(dto.getReviewPeriod());
        review.setStatus(dto.getStatus());
        review.setOverallScore(dto.getOverallScore());
        review.setComments(dto.getComments());
        
        PerformanceReview saved = performanceReviewRepository.save(review);
        return mapReviewToDTO(saved);
    }

    @Transactional
    public PerformanceReviewDTO updateReview(Long id, PerformanceReviewDTO dto) {
        PerformanceReview review = performanceReviewRepository.findById(id).orElseThrow();
        review.setReviewPeriod(dto.getReviewPeriod());
        review.setStatus(dto.getStatus());
        review.setOverallScore(dto.getOverallScore());
        review.setComments(dto.getComments());
        
        if (dto.getReviewerId() != null && (review.getReviewer() == null || !review.getReviewer().getId().equals(dto.getReviewerId()))) {
            Employee reviewer = employeeRepository.findById(dto.getReviewerId()).orElseThrow();
            review.setReviewer(reviewer);
        }
        
        PerformanceReview saved = performanceReviewRepository.save(review);
        return mapReviewToDTO(saved);
    }

    public void deleteReview(Long id) {
        performanceReviewRepository.deleteById(id);
    }

    private KpiDTO mapKpiToDTO(Kpi kpi) {
        KpiDTO dto = new KpiDTO();
        dto.setId(kpi.getId());
        if (kpi.getEmployee() != null) {
            dto.setEmployeeId(kpi.getEmployee().getId());
            dto.setEmployeeName(kpi.getEmployee().getFullName().getFullName());
        }
        dto.setTitle(kpi.getTitle());
        dto.setDescription(kpi.getDescription());
        dto.setTargetValue(kpi.getTargetValue());
        dto.setActualValue(kpi.getActualValue());
        dto.setStatus(kpi.getStatus());
        dto.setStartDate(kpi.getStartDate());
        dto.setEndDate(kpi.getEndDate());
        dto.setWeight(kpi.getWeight());
        dto.setCreatedAt(kpi.getCreatedAt());
        dto.setUpdatedAt(kpi.getUpdatedAt());
        return dto;
    }

    private PerformanceReviewDTO mapReviewToDTO(PerformanceReview review) {
        PerformanceReviewDTO dto = new PerformanceReviewDTO();
        dto.setId(review.getId());
        if (review.getEmployee() != null) {
            dto.setEmployeeId(review.getEmployee().getId());
            dto.setEmployeeName(review.getEmployee().getFullName().getFullName());
        }
        if (review.getReviewer() != null) {
            dto.setReviewerId(review.getReviewer().getId());
            dto.setReviewerName(review.getReviewer().getFullName().getFullName());
        }
        dto.setReviewPeriod(review.getReviewPeriod());
        dto.setStatus(review.getStatus());
        dto.setOverallScore(review.getOverallScore());
        dto.setComments(review.getComments());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }
}
