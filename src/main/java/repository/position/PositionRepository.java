package repository.position;

import model.position.Position;
import model.position.PositionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    Optional<Position> findByPositionId(PositionId positionId);
    boolean existsByPositionId(PositionId positionId);
}
