package service.department;

import com.hr.dto.DepartmentRequest;
import com.hr.dto.DepartmentResponse;
import model.department.Department;
import model.department.DepartmentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.department.DepartmentRepository;
import repository.position.PositionRepository;
import model.position.Position;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    public DepartmentService(DepartmentRepository departmentRepository, PositionRepository positionRepository) {
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::new)
                .collect(Collectors.toList());
    }

    public DepartmentResponse getDepartmentById(String id) {
        Department department = findDepartmentEntity(id);
        return new DepartmentResponse(department);
    }

    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.findByDepartmentIdValue(request.getDepartmentId().toUpperCase()).isPresent()) {
            throw new IllegalArgumentException("Department ID already exists");
        }
        Position headPosition = null;
        if (request.getHeadPositionId() != null && !request.getHeadPositionId().isBlank()) {
            headPosition = positionRepository.findByPositionId(new model.position.PositionId(request.getHeadPositionId().toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException("Position not found: " + request.getHeadPositionId()));
        }

        Department department = new Department(
                new DepartmentId(request.getDepartmentId()),
                request.getName(),
                request.getDescription(),
                headPosition
        );
        return new DepartmentResponse(departmentRepository.save(department));
    }

    public DepartmentResponse updateDepartment(String id, DepartmentRequest request) {
        Department department = findDepartmentEntity(id);
        
        Position headPosition = null;
        if (request.getHeadPositionId() != null && !request.getHeadPositionId().isBlank()) {
            headPosition = positionRepository.findByPositionId(new model.position.PositionId(request.getHeadPositionId().toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException("Position not found: " + request.getHeadPositionId()));
        }
        
        department.updateDetails(request.getName(), request.getDescription(), headPosition);
        return new DepartmentResponse(departmentRepository.save(department));
    }

    @SuppressWarnings("null")
    public void deleteDepartment(String id) {
        Department department = findDepartmentEntity(id);
        departmentRepository.delete(department);
    }

    public Department findDepartmentEntity(String id) {
        return departmentRepository.findByDepartmentIdValue(id.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));
    }
}
