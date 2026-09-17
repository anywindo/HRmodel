package repository.employee;

import model.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeId(String employeeId);
    java.util.List<Employee> findByPosition_Department_DepartmentIdValue(String departmentId);
    java.util.List<Employee> findByPosition_PositionId(model.position.PositionId positionId);
    java.util.List<Employee> findByPosition_PositionId_Value(String positionIdValue);
    Optional<Employee> findByEmail_Value(String email);
    long countByRoles_Id(Long roleId);
    java.util.List<Employee> findByRoles_Name(String roleName);
}
