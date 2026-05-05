package naranhi.backend.domain.notification.repository;

import naranhi.backend.domain.notification.entity.DeviceNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeviceNotificationRepository extends JpaRepository<DeviceNotification, Long> {

    @Query("""
            SELECT dn FROM DeviceNotification dn
            JOIN FETCH dn.device
            WHERE dn.notification.id IN :notificationIds
            """)
    List<DeviceNotification> findByNotificationIds(@Param("notificationIds") List<Long> notificationIds);
}
