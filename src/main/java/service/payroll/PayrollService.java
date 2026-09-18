package service.payroll;

import model.employee.Employee;
import model.employee.EmployeeStatus;
import model.payroll.PayrollItem;
import model.payroll.PayrollRun;
import model.payroll.PayrollStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.employee.EmployeeRepository;
import repository.payroll.PayrollRunRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PayrollService {

    private final PayrollRunRepository payrollRunRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollCalculationService calculationService;

    public PayrollService(PayrollRunRepository payrollRunRepository, 
                          EmployeeRepository employeeRepository,
                          PayrollCalculationService calculationService) {
        this.payrollRunRepository = payrollRunRepository;
        this.employeeRepository = employeeRepository;
        this.calculationService = calculationService;
    }

    public List<PayrollRun> getAllRuns() {
        return payrollRunRepository.findAll();
    }

    public PayrollRun getRun(Long id) {
        if (id == null) throw new IllegalArgumentException("ID must not be null");
        return payrollRunRepository.findById(id).orElseThrow(() -> new RuntimeException("Payroll Run not found"));
    }

    public PayrollRun generateDraftRun(String periodName, LocalDate periodStart, LocalDate periodEnd, Long creatorId) {
        if (creatorId == null) throw new IllegalArgumentException("Creator ID must not be null");
        Employee creator = employeeRepository.findById(creatorId).orElseThrow();
        PayrollRun run = new PayrollRun(periodName, periodStart, periodEnd, creator);
        
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .toList();

        for (Employee emp : activeEmployees) {
            PayrollItem item = new PayrollItem(emp);
            
            // In a real system, baseSalary would be prorated based on attendance and hireDate
            item.setBaseSalary(emp.getSalary().getAmount());
            
            // Fetch attendance data here to compute overtime and absence deductions
            // For now, these are 0
            
            calculationService.calculateTaxesAndDeductions(item);
            run.addItem(item);
        }

        return payrollRunRepository.save(run);
    }

    public PayrollRun submitForReview(Long runId) {
        PayrollRun run = getRun(runId);
        if (run.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT runs can be submitted for review");
        }
        run.setStatus(PayrollStatus.REVIEW);
        return payrollRunRepository.save(run);
    }

    public PayrollRun approveRun(Long runId) {
        PayrollRun run = getRun(runId);
        if (run.getStatus() != PayrollStatus.REVIEW) {
            throw new IllegalStateException("Only REVIEW runs can be approved");
        }
        run.setStatus(PayrollStatus.APPROVED);
        return payrollRunRepository.save(run);
    }

    public PayrollRun finalizeRun(Long runId) {
        PayrollRun run = getRun(runId);
        if (run.getStatus() != PayrollStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED runs can be finalized");
        }
        run.setStatus(PayrollStatus.FINALIZED);
        return payrollRunRepository.save(run);
    }

    public PayrollItem getPayrollItem(Long itemId) {
        // Find the item across all runs, or just add PayrollItemRepository
        // We'll search in all runs for simplicity or add a repository. 
        // Adding a repository is better, but since it's an entity, let's just query it.
        // Actually I don't have PayrollItemRepository. I will just do a manual search for now.
        return payrollRunRepository.findAll().stream()
                .flatMap(run -> run.getItems().stream())
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payroll item not found"));
    }
    
    public PayrollRun updateStatus(Long id, PayrollStatus status) {
        PayrollRun run = getRun(id);
        run.setStatus(status);
        return payrollRunRepository.save(run);
    }
}
