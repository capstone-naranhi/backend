package naranhi.backend.notification.repository;

import java.util.List;
import java.util.Optional;
import naranhi.backend.notification.entity.DeviceNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceNotificationRepository extends JpaRepository<DeviceNotification, Long> {

    @Query("""
            SELECT dn FROM DeviceNotification dn
            JOIN FETCH dn.device
            WHERE dn.notification.id IN :notificationIds
            """)
    List<DeviceNotification> findByNotificationIds(@Param("notificationIds") List<Long> notificationIds);

    @Query("""
            SELECT dn FROM DeviceNotification dn
            JOIN FETCH dn.device
            WHERE dn.notification.id = :notificationId
            """)
    Optional<DeviceNotification> findByNotificationId(@Param("notificationId") Long notificationId);
}
