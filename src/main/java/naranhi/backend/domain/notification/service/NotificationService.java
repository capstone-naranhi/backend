package naranhi.backend.domain.notification.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import naranhi.backend.domain.notification.dto.NotificationResponse;
import naranhi.backend.domain.notification.entity.DeviceNotification;
import naranhi.backend.domain.notification.entity.GeneralNotification;
import naranhi.backend.domain.notification.entity.NotificationRecipient;
import naranhi.backend.domain.notification.entity.NotificationType;
import naranhi.backend.domain.notification.entity.SafetyNotification;
import naranhi.backend.domain.notification.repository.DeviceNotificationRepository;
import naranhi.backend.domain.notification.repository.GeneralNotificationRepository;
import naranhi.backend.domain.notification.repository.NotificationRecipientRepository;
import naranhi.backend.domain.notification.repository.SafetyNotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final int PAGE_SIZE = 20;

    private final NotificationRecipientRepository notificationRecipientRepository;
    private final SafetyNotificationRepository safetyNotificationRepository;
    private final DeviceNotificationRepository deviceNotificationRepository;
    private final GeneralNotificationRepository generalNotificationRepository;

    public NotificationResponse.UnreadCount getUnreadCount(Long memberId) {
        long count = notificationRecipientRepository.countUnreadByMemberId(memberId);
        return new NotificationResponse.UnreadCount(count);
    }

    public NotificationResponse.NotificationList getNotificationList(
            Long memberId, NotificationType type, Long cursorId
    ) {
        List<NotificationRecipient> fetchedNotification = (type == null)
                ? notificationRecipientRepository.findByMemberWithCursor(memberId, cursorId,
                PageRequest.of(0, PAGE_SIZE + 1))
                : notificationRecipientRepository.findByMemberAndTypeWithCursor(memberId, type, cursorId,
                        PageRequest.of(0, PAGE_SIZE + 1));

        boolean hasNext = fetchedNotification.size() > PAGE_SIZE;
        List<NotificationRecipient> pageNotification =
                hasNext ? fetchedNotification.subList(0, PAGE_SIZE) : fetchedNotification;

        Map<Long, SafetyNotification> safetyMap = fetchSafetyMap(pageNotification);
        Map<Long, DeviceNotification> deviceMap = fetchDeviceMap(pageNotification);
        Map<Long, GeneralNotification> generalMap = fetchGeneralMap(pageNotification);

        List<NotificationResponse.NotificationItem> items = pageNotification.stream()
                .map(nr -> toItem(nr, safetyMap, deviceMap, generalMap))
                .toList();

        Long nextCursorId = hasNext ? pageNotification.getLast().getNotification().getId() : null;

        return new NotificationResponse.NotificationList(items, nextCursorId, hasNext);
    }

    private Map<Long, SafetyNotification> fetchSafetyMap(
            List<NotificationRecipient> page
    ) {
        List<Long> safetyIds = filterNotificationType(page, NotificationType.SAFETY);
        if (safetyIds.isEmpty()) {
            return Map.of();
        }
        return safetyNotificationRepository.findByNotificationIds(safetyIds).stream()
                .collect(Collectors.toMap(sn -> sn.getNotification().getId(), sn -> sn));
    }

    private Map<Long, DeviceNotification> fetchDeviceMap(
            List<NotificationRecipient> page
    ) {
        List<Long> deviceIds = filterNotificationType(page, NotificationType.DEVICE);
        if (deviceIds.isEmpty()) {
            return Map.of();
        }
        return deviceNotificationRepository.findByNotificationIds(deviceIds).stream()
                .collect(Collectors.toMap(dn -> dn.getNotification().getId(), dn -> dn));
    }

    private Map<Long, GeneralNotification> fetchGeneralMap(
            List<NotificationRecipient> page
    ) {
        List<Long> generalIds = filterNotificationType(page, NotificationType.GENERAL);
        if (generalIds.isEmpty()) {
            return Map.of();
        }
        return generalNotificationRepository.findByNotificationIds(generalIds).stream()
                .collect(Collectors.toMap(gn -> gn.getNotification().getId(), gn -> gn));
    }

    private List<Long> filterNotificationType(List<NotificationRecipient> page, NotificationType target) {
        return page.stream()
                .filter(nr -> nr.getNotification().getType() == target)
                .map(nr -> nr.getNotification().getId())
                .toList();
    }

    private NotificationResponse.NotificationItem toItem(
            NotificationRecipient nr,
            Map<Long, SafetyNotification> safetyMap,
            Map<Long, DeviceNotification> deviceMap,
            Map<Long, GeneralNotification> generalMap
    ) {
        Long notificationId = nr.getNotification().getId();
        NotificationType type = nr.getNotification().getType();

        NotificationResponse.SafetyDetail safetyDetail = null;
        NotificationResponse.DeviceDetail deviceDetail = null;
        NotificationResponse.GeneralDetail generalDetail = null;

        switch (type) {
            case SAFETY -> {
                SafetyNotification sn = safetyMap.get(notificationId);
                if (sn != null) {
                    safetyDetail = new NotificationResponse.SafetyDetail(
                            sn.getDevice().getDeviceName(),
                            sn.getEventType(),
                            sn.getSeverity(),
                            sn.getSafetyEvent().getDurationSecond()
                    );
                }
            }
            case DEVICE -> {
                DeviceNotification dn = deviceMap.get(notificationId);
                if (dn != null) {
                    deviceDetail = new NotificationResponse.DeviceDetail(
                            dn.getDevice().getDeviceName(),
                            dn.getComponentType(),
                            dn.getBeforeStatus(),
                            dn.getCurrentStatus(),
                            dn.getDescription()
                    );
                }
            }
            case GENERAL -> {
                GeneralNotification gn = generalMap.get(notificationId);
                if (gn != null) {
                    generalDetail = new NotificationResponse.GeneralDetail(
                            gn.getDetailType(),
                            gn.getTitle()
                    );
                }
            }
        }

        return new NotificationResponse.NotificationItem(
                notificationId,
                type,
                nr.getNotification().getSentAt(),
                nr.isRead(),
                safetyDetail,
                deviceDetail,
                generalDetail
        );
    }
}
