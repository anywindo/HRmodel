package service.position;

import com.hr.dto.PositionRequest;
import com.hr.dto.PositionResponse;
import model.department.Department;
import model.position.Position;
import model.position.PositionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.department.DepartmentRepository;
import repository.position.PositionRepository;
import repository.employee.EmployeeRepository;
import model.employee.EmployeeStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public PositionService(PositionRepository positionRepository, DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.positionRepository = positionRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<PositionResponse> getAllPositions() {
        return positionRepository.findAll().stream()
                .map(pos -> {
                    PositionResponse res = new PositionResponse(pos);
                    employeeRepository.findByPosition_PositionId(pos.getPositionId()).stream()
                            .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                            .findFirst()
                            .ifPresent(e -> res.setOccupantName(e.getFullName().getFirstName() + " " + e.getFullName().getLastName()));
                    return res;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PositionResponse getPositionById(String id) {
        Position position = positionRepository.findByPositionId(new PositionId(id.toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Position not found: " + id));
        PositionResponse res = new PositionResponse(position);
        employeeRepository.findByPosition_PositionId(position.getPositionId()).stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .findFirst()
                .ifPresent(e -> res.setOccupantName(e.getFullName().getFirstName() + " " + e.getFullName().getLastName()));
        return res;
    }

    public PositionResponse createPosition(PositionRequest request) {
        if (request.getPositionId() == null || request.getPositionId().isBlank()) {
            throw new IllegalArgumentException("Position ID is required");
        }
        
        PositionId positionId = new PositionId(request.getPositionId().toUpperCase());
        if (positionRepository.findByPositionId(positionId).isPresent()) {
            throw new IllegalArgumentException("Position ID already exists");
        }

        Department department = null;
        if (request.getDepartmentId() != null && !request.getDepartmentId().isBlank()) {
            department = departmentRepository.findByDepartmentIdValue(request.getDepartmentId().toUpperCase())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        } else {
            throw new IllegalArgumentException("Department is required for a position");
        }
        
        Position reportsTo = null;
        if (request.getReportsToId() != null && !request.getReportsToId().isBlank()) {
            reportsTo = positionRepository.findByPositionId(new PositionId(request.getReportsToId().toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException("ReportsTo Position not found"));
        }

        Position position = new Position(
                positionId,
                request.getTitle(),
                request.getDescription(),
                department,
                reportsTo
        );

        return new PositionResponse(positionRepository.save(position));
    }

    public PositionResponse updatePosition(String id, PositionRequest request) {
        Position position = positionRepository.findByPositionId(new PositionId(id.toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Position not found: " + id));

        Department department = null;
        if (request.getDepartmentId() != null && !request.getDepartmentId().isBlank()) {
            department = departmentRepository.findByDepartmentIdValue(request.getDepartmentId().toUpperCase())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        } else {
            throw new IllegalArgumentException("Department is required for a position");
        }
        
        Position reportsTo = null;
        if (request.getReportsToId() != null && !request.getReportsToId().isBlank()) {
            reportsTo = positionRepository.findByPositionId(new PositionId(request.getReportsToId().toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException("ReportsTo Position not found"));
        }

        position.updateDetails(request.getTitle(), request.getDescription(), department, reportsTo);

        return new PositionResponse(positionRepository.save(position));
    }

    @SuppressWarnings("null")
    public void deletePosition(String id) {
        Position position = positionRepository.findByPositionId(new PositionId(id.toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Position not found: " + id));
        positionRepository.deleteById(position.getId());
    }
}
