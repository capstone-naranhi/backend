package naranhi.backend.mqtt.processor;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naranhi.backend.domain.device.entity.Device;
import naranhi.backend.domain.device.repository.DeviceRepository;
import naranhi.backend.domain.member.entity.Member;
import naranhi.backend.domain.member.repository.MemberRepository;
import naranhi.backend.domain.notification.entity.ComponentStatus;
import naranhi.backend.domain.notification.entity.ComponentType;
import naranhi.backend.domain.notification.entity.DeviceNotification;
import naranhi.backend.domain.notification.entity.Notification;
import naranhi.backend.domain.notification.entity.NotificationRecipient;
import naranhi.backend.domain.notification.entity.NotificationType;
import naranhi.backend.domain.notification.repository.DeviceNotificationRepository;
import naranhi.backend.domain.notification.repository.NotificationRecipientRepository;
import naranhi.backend.domain.notification.repository.NotificationRepository;
import naranhi.backend.fcm.FcmPayload;
import naranhi.backend.fcm.FcmService;
import naranhi.backend.log.service.MongoLogService;
import naranhi.backend.mqtt.dto.StatusChangeMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusChangeProcessor {

    private final DeviceRepository deviceRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final DeviceNotificationRepository deviceNotificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final FcmService fcmService;
    private final MongoLogService mongoLogService;

    public void process(StatusChangeMessage message) {
        Device device = deviceRepository.findByDeviceSerialNumber(message.deviceSerial())
                .orElseThrow(() -> new IllegalArgumentException("미등록 장치: " + message.deviceSerial()));

        List<Long> memberIds = deviceRepository.findMemberIdsByDeviceSerialNumber(message.deviceSerial());
        if (memberIds.isEmpty()) {
            log.warn("연결된 회원 없음 - serial: {}", message.deviceSerial());
            return;
        }

        ComponentType componentType;
        ComponentStatus previousStatus;
        ComponentStatus currentStatus;
        try {
            componentType = ComponentType.valueOf(message.componentType());
            previousStatus = ComponentStatus.valueOf(message.previousStatus());
            currentStatus = ComponentStatus.valueOf(message.currentStatus());
        } catch (IllegalArgumentException e) {
            log.error("알 수 없는 ComponentType/Status - serial: {}, component: {}, {} → {}",
                    message.deviceSerial(), message.componentType(),
                    message.previousStatus(), message.currentStatus());
            return;
        }

        Long notifId = saveMysql(message, device, memberIds, componentType, previousStatus, currentStatus);

        FcmPayload payload = FcmPayload.ofDevice(
                device.getDeviceName(),
                message.componentType(),
                message.previousStatus(),
                message.currentStatus(),
                notifId
        );
        fcmService.sendToMembers(memberIds, payload, NotificationType.DEVICE);

        mongoLogService.saveDeviceStatusLog(message, device);

        log.info("상태 변경 처리 완료 - serial: {}, {} {} → {}",
                message.deviceSerial(), componentType, previousStatus, currentStatus);
    }

    @Transactional
    public Long saveMysql(
            StatusChangeMessage message,
            Device device,
            List<Long> memberIds,
            ComponentType componentType,
            ComponentStatus previousStatus,
            ComponentStatus currentStatus
    ) {
        device.updateComponentStatus(componentType, currentStatus);
        deviceRepository.save(device);

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .type(NotificationType.DEVICE)
                        .sentAt(java.time.LocalDateTime.now())
                        .build()
        );

        deviceNotificationRepository.save(
                DeviceNotification.builder()
                        .notification(notification)
                        .device(device)
                        .componentType(componentType)
                        .beforeStatus(previousStatus)
                        .currentStatus(currentStatus)
                        .description(message.reason())
                        .build()
        );

        List<Member> members = memberRepository.findAllById(memberIds);
        notificationRecipientRepository.saveAll(
                members.stream()
                        .map(member -> NotificationRecipient.builder()
                                .notification(notification)
                                .member(member)
                                .isRead(false)
                                .isSent(false)
                                .build()
                        )
                        .toList()
        );

        return notification.getId();
    }
}
