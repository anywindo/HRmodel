package repository.leave;

import model.leave.LeaveDelegation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveDelegationRepository extends JpaRepository<LeaveDelegation, String> {
    
    @Query("SELECT d FROM LeaveDelegation d WHERE d.delegatee.employeeId = :delegateeId AND d.active = true AND d.startDate <= :today AND d.endDate >= :today")
    List<LeaveDelegation> findActiveDelegationsForDelegatee(@Param("delegateeId") String delegateeId, @Param("today") LocalDate today);

    @Query("SELECT d FROM LeaveDelegation d WHERE d.active = true AND d.startDate <= :today AND d.endDate >= :today")
    List<LeaveDelegation> findAllActiveDelegations(@Param("today") LocalDate today);

    List<LeaveDelegation> findByDelegator_EmployeeIdOrderByStartDateDesc(String delegatorId);

    List<LeaveDelegation> findByDelegatee_EmployeeIdOrderByStartDateDesc(String delegateeId);
}
