package naranhi.backend.domain.notification.repository;

import naranhi.backend.domain.notification.entity.SafetyNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SafetyNotificationRepository extends JpaRepository<SafetyNotification, Long> {

    @Query("""
            SELECT sn FROM SafetyNotification sn
            JOIN FETCH sn.device
            JOIN FETCH sn.safetyEvent
            WHERE sn.notification.id IN :notificationIds
            """)
    List<SafetyNotification> findByNotificationIds(@Param("notificationIds") List<Long> notificationIds);

    @Query("""
            SELECT sn FROM SafetyNotification sn
            JOIN FETCH sn.device
            JOIN FETCH sn.safetyEvent
            WHERE sn.notification.id = :notificationId
            """)
    Optional<SafetyNotification> findByNotificationId(@Param("notificationId") Long notificationId);
}
