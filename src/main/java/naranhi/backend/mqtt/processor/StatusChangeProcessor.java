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
        Device device = deviceRepository.findByDeviceSerialNumber(message.deviceSerialNumber())
                .orElseThrow(() -> new IllegalArgumentException("미등록 장치: " + message.deviceSerialNumber()));

        List<Long> memberIds = deviceRepository.findMemberIdsByDeviceSerialNumber(message.deviceSerialNumber());
        if (memberIds.isEmpty()) {
            log.warn("연결된 회원 없음 - deviceSerialNumber: {}", message.deviceSerialNumber());
            return;
        }

        // ComponentType, ComponentStatus 파싱
        ComponentType componentType;
        ComponentStatus beforeStatus;
        ComponentStatus currentStatus;
        try {
            componentType = ComponentType.valueOf(message.componentType());
            beforeStatus = ComponentStatus.valueOf(message.beforeStatus());
            currentStatus = ComponentStatus.valueOf(message.currentStatus());
        } catch (IllegalArgumentException e) {
            log.error("알 수 없는 ComponentType/Status - {}", message);
            return;
        }

        // MySQL 트랜잭션
        Long notifId = saveMysql(message, device, memberIds, componentType, beforeStatus, currentStatus);

        // FCM 전송
        FcmPayload payload = FcmPayload.ofDevice(
                device.getDeviceName(),
                message.componentType(),
                message.beforeStatus(),
                message.currentStatus(),
                notifId
        );
        fcmService.sendToMembers(memberIds, payload, NotificationType.DEVICE);

        // MongoDB 비동기
        mongoLogService.saveDeviceStatusLog(message, device);

        log.info("상태 변경 처리 완료 - deviceSerialNumber: {}, {} {} → {}",
                message.deviceSerialNumber(), componentType, beforeStatus, currentStatus);
    }

    @Transactional
    public Long saveMysql(
            StatusChangeMessage message,
            Device device,
            List<Long> memberIds,
            ComponentType componentType,
            ComponentStatus beforeStatus,
            ComponentStatus currentStatus
    ) {

        // ① Device 컴포넌트 상태 UPDATE
        device.updateComponentStatus(componentType, currentStatus);
        deviceRepository.save(device);

        // ② Notification INSERT
        Notification notification = notificationRepository.save(
                Notification.builder()
                        .type(NotificationType.DEVICE)
                        .sentAt(java.time.LocalDateTime.now())
                        .build()
        );

        // ③ DeviceNotification INSERT
        deviceNotificationRepository.save(
                DeviceNotification.builder()
                        .notification(notification)
                        .device(device)
                        .componentType(componentType)
                        .beforeStatus(beforeStatus)
                        .currentStatus(currentStatus)
                        .description(message.description())
                        .build()
        );

        // ④ NotificationRecipient INSERT
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
