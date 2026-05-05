package naranhi.backend.domain.safety.repository;

import naranhi.backend.domain.safety.entity.SafetyEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SafetyEventRepository extends JpaRepository<SafetyEvent, Long> {

    Optional<SafetyEvent> findTopByDeviceIdOrderByDetectedAtDesc(Long deviceId);
}
