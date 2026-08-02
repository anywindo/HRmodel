package service.department;

import com.hr.dto.DepartmentRequest;
import com.hr.dto.DepartmentResponse;
import model.department.Department;
import model.department.DepartmentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.department.DepartmentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
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
        Department department = new Department(
                new DepartmentId(request.getDepartmentId()),
                request.getName(),
                request.getDescription()
        );
        return new DepartmentResponse(departmentRepository.save(department));
    }

    public DepartmentResponse updateDepartment(String id, DepartmentRequest request) {
        Department department = findDepartmentEntity(id);
        department.updateDetails(request.getName(), request.getDescription());
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
