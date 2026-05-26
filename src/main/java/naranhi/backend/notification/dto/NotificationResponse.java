package naranhi.backend.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import naranhi.backend.notification.entity.ComponentStatus;
import naranhi.backend.notification.entity.ComponentType;
import naranhi.backend.notification.entity.DeviceNotification;
import naranhi.backend.notification.entity.GeneralNotification;
import naranhi.backend.notification.entity.GeneralNotificationDetailType;
import naranhi.backend.notification.entity.NotificationRecipient;
import naranhi.backend.notification.entity.NotificationType;
import naranhi.backend.notification.entity.SafetyNotification;
import naranhi.backend.safety.dto.EventTypeResponse;
import naranhi.backend.safety.entity.Severity;

public class NotificationResponse {

    public record NotificationList(
            List<NotificationItem> notifications,
            Long nextCursorId,
            boolean hasNext
    ) {
        public static NotificationList of(List<NotificationItem> items, Long nextCursorId, boolean hasNext) {
            return new NotificationList(items, nextCursorId, hasNext);
        }
    }

    public record UnreadCount(long unreadCount) {
        public static UnreadCount from(long count) {
            return new UnreadCount(count);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record NotificationItem(
            Long notificationId,
            NotificationType type,
            LocalDateTime sentAt,
            boolean isRead,
            SafetyDetail safetyDetail,
            DeviceDetail deviceDetail,
            GeneralDetail generalDetail
    ) {
        public static NotificationItem of(
                NotificationRecipient nr,
                SafetyDetail safetyDetail,
                DeviceDetail deviceDetail,
                GeneralDetail generalDetail
        ) {
            return new NotificationItem(
                    nr.getNotification().getId(),
                    nr.getNotification().getType(),
                    nr.getNotification().getSentAt(),
                    nr.isRead(),
                    safetyDetail,
                    deviceDetail,
                    generalDetail
            );
        }
    }

    public record SafetyDetail(
            String deviceName,
            EventTypeResponse eventType,
            Severity severity,
            Integer durationSecond
    ) {
        public static SafetyDetail from(SafetyNotification sn) {
            return new SafetyDetail(
                    sn.getDevice().getDeviceName(),
                    EventTypeResponse.from(sn.getEventType()),
                    sn.getSeverity(),
                    sn.getSafetyEvent().getDurationSecond()
            );
        }
    }

    public record DeviceDetail(
            Long deviceId,
            String deviceName,
            ComponentType componentType,
            ComponentStatus beforeStatus,
            ComponentStatus currentStatus,
            String description
    ) {
        public static DeviceDetail from(DeviceNotification dn) {
            return new DeviceDetail(
                    dn.getDevice().getId(),
                    dn.getDevice().getDeviceName(),
                    dn.getComponentType(),
                    dn.getBeforeStatus(),
                    dn.getCurrentStatus(),
                    dn.getDescription()
            );
        }
    }

    public record GeneralDetail(
            GeneralNotificationDetailType detailType,
            String title
    ) {
        public static GeneralDetail from(GeneralNotification gn) {
            return new GeneralDetail(gn.getDetailType(), gn.getTitle());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record NotificationDetail(
            Long notificationId,
            NotificationType type,
            LocalDateTime sentAt,
            boolean isRead,
            SafetyNotificationDetail safetyDetail,
            DeviceDetail deviceDetail,
            GeneralNotificationDetail generalDetail
    ) {
        public static NotificationDetail of(
                NotificationRecipient nr,
                SafetyNotificationDetail safetyDetail,
                DeviceDetail deviceDetail,
                GeneralNotificationDetail generalDetail
        ) {
            return new NotificationDetail(
                    nr.getNotification().getId(),
                    nr.getNotification().getType(),
                    nr.getNotification().getSentAt(),
                    nr.isRead(),
                    safetyDetail,
                    deviceDetail,
                    generalDetail
            );
        }
    }

    public record SafetyNotificationDetail(
            Long deviceId,
            String deviceName,
            EventTypeResponse eventType,
            Severity severity,
            Integer durationSecond,
            BigDecimal confidence,
            LocalDateTime detectedAt,
            String snapshotUrl,
            String videoUrl
    ) {
        public static SafetyNotificationDetail from(SafetyNotification sn) {
            return new SafetyNotificationDetail(
                    sn.getDevice().getId(),
                    sn.getDevice().getDeviceName(),
                    EventTypeResponse.from(sn.getEventType()),
                    sn.getSeverity(),
                    sn.getSafetyEvent().getDurationSecond(),
                    sn.getSafetyEvent().getConfidence(),
                    sn.getSafetyEvent().getDetectedAt(),
                    sn.getSafetyEvent().getSnapshotUrl(),
                    sn.getSafetyEvent().getVideoUrl()
            );
        }
    }

    public record GeneralNotificationDetail(
            GeneralNotificationDetailType detailType,
            String title,
            String content
    ) {
        public static GeneralNotificationDetail from(GeneralNotification gn) {
            return new GeneralNotificationDetail(gn.getDetailType(), gn.getTitle(), gn.getContent());
        }
    }
}
