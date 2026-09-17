package repository.leave;

import model.leave.EmployeeLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeLeaveBalanceRepository extends JpaRepository<EmployeeLeaveBalance, String> {
    Optional<EmployeeLeaveBalance> findByEmployee_EmployeeId(String employeeId);
}
