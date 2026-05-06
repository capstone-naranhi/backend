package naranhi.backend.domain.notification.repository;

import naranhi.backend.domain.notification.entity.NotificationRecipient;
import naranhi.backend.domain.notification.entity.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {

    @Query("""
            SELECT nr FROM NotificationRecipient nr
            JOIN FETCH nr.notification n
            WHERE nr.member.id = :memberId
            AND (:cursorId IS NULL OR n.id < :cursorId)
            ORDER BY n.sentAt DESC, n.id DESC
            """)
    List<NotificationRecipient> findByMemberWithCursor(
            @Param("memberId") Long memberId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT nr FROM NotificationRecipient nr
            JOIN FETCH nr.notification n
            WHERE nr.member.id = :memberId
            AND n.type = :type
            AND (:cursorId IS NULL OR n.id < :cursorId)
            ORDER BY n.sentAt DESC, n.id DESC
            """)
    List<NotificationRecipient> findByMemberAndTypeWithCursor(
            @Param("memberId") Long memberId,
            @Param("type") NotificationType type,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(nr) FROM NotificationRecipient nr
            WHERE nr.member.id = :memberId
            AND nr.isRead = false
            """)
    long countUnreadByMemberId(@Param("memberId") Long memberId);

    @Query("""
            SELECT COUNT(nr) FROM NotificationRecipient nr
            WHERE nr.member.id = :memberId
            AND nr.notification.sentAt >= :start
            AND nr.notification.sentAt < :end
            """)
    long countTodayByMemberId(
            @Param("memberId") Long memberId,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end
    );

    @Query("""
            SELECT nr FROM NotificationRecipient nr
            WHERE nr.notification.id = :notificationId
            AND nr.member.id = :memberId
            """)
    java.util.Optional<NotificationRecipient> findByNotificationIdAndMemberId(
            @Param("notificationId") Long notificationId,
            @Param("memberId") Long memberId
    );
}
