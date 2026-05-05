package naranhi.backend.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import naranhi.backend.domain.notification.entity.ComponentStatus;
import naranhi.backend.domain.notification.entity.ComponentType;
import naranhi.backend.domain.notification.entity.GeneralNotificationDetailType;
import naranhi.backend.domain.notification.entity.NotificationType;
import naranhi.backend.domain.safety.entity.EventType;
import naranhi.backend.domain.safety.entity.Severity;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationResponse {

    public record NotificationList(
            List<NotificationItem> notifications,
            Long nextCursorId,
            boolean hasNext
    ) {}

    public record UnreadCount(long unreadCount) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record NotificationItem(
            Long notificationId,
            NotificationType type,
            LocalDateTime sentAt,
            boolean isRead,
            SafetyDetail safetyDetail,
            DeviceDetail deviceDetail,
            GeneralDetail generalDetail
    ) {}

    public record SafetyDetail(
            String deviceName,
            EventType eventType,
            Severity severity,
            Integer durationSecond
    ) {}

    public record DeviceDetail(
            String deviceName,
            ComponentType componentType,
            ComponentStatus beforeStatus,
            ComponentStatus currentStatus,
            String description
    ) {}

    public record GeneralDetail(
            GeneralNotificationDetailType detailType,
            String title
    ) {}
}
