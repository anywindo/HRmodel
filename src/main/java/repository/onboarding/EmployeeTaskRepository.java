package repository.onboarding;

import model.onboarding.EmployeeTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeTaskRepository extends JpaRepository<EmployeeTask, Long> {
    
    List<EmployeeTask> findByEmployeeId(Long employeeId);

    @Query("SELECT et FROM EmployeeTask et JOIN et.templateTask tt WHERE tt.assignedRole = :role")
    List<EmployeeTask> findByAssignedRole(@Param("role") String role);

}
