package naranhi.backend.domain.notification.repository;

import naranhi.backend.domain.notification.entity.GeneralNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GeneralNotificationRepository extends JpaRepository<GeneralNotification, Long> {

    @Query("""
            SELECT gn FROM GeneralNotification gn
            WHERE gn.notification.id IN :notificationIds
            """)
    List<GeneralNotification> findByNotificationIds(@Param("notificationIds") List<Long> notificationIds);

    Optional<GeneralNotification> findByNotificationId(Long notificationId);
}
