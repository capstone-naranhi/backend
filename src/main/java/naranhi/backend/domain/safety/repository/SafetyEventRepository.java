package naranhi.backend.domain.safety.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import naranhi.backend.domain.safety.entity.EventType;
import naranhi.backend.domain.safety.entity.SafetyEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SafetyEventRepository extends JpaRepository<SafetyEvent, Long> {

    Optional<SafetyEvent> findTopByDeviceIdOrderByDetectedAtDesc(Long deviceId);

    Optional<SafetyEvent> findTopByDevice_DeviceSerialNumberOrderByDetectedAtDesc(String deviceSerial);

    Optional<SafetyEvent> findTopByDeviceIdInOrderByDetectedAtDesc(List<Long> deviceIds);

    @Query("""
            SELECT COUNT(se) FROM SafetyEvent se
            WHERE se.device.id IN :deviceIds
            AND se.eventType = :eventType
            AND se.detectedAt >= :start
            AND se.detectedAt < :end
            """)
    long countByDeviceIdsAndEventTypeAndDetectedAtBetween(
            @Param("deviceIds") List<Long> deviceIds,
            @Param("eventType") EventType eventType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
