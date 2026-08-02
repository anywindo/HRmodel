package repository.department;

import model.department.Department;
import model.department.DepartmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDepartmentId(DepartmentId departmentId);
    
    // Spring Data JPA can traverse embedded properties if needed:
    Optional<Department> findByDepartmentIdValue(String value);
}
