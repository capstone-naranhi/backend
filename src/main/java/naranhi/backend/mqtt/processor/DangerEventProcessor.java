package naranhi.backend.mqtt.processor;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naranhi.backend.domain.device.entity.Device;
import naranhi.backend.domain.device.repository.DeviceRepository;
import naranhi.backend.domain.member.entity.Member;
import naranhi.backend.domain.member.repository.MemberRepository;
import naranhi.backend.domain.notification.entity.Notification;
import naranhi.backend.domain.notification.entity.NotificationRecipient;
import naranhi.backend.domain.notification.entity.NotificationType;
import naranhi.backend.domain.notification.entity.SafetyNotification;
import naranhi.backend.domain.notification.repository.NotificationRecipientRepository;
import naranhi.backend.domain.notification.repository.NotificationRepository;
import naranhi.backend.domain.notification.repository.SafetyNotificationRepository;
import naranhi.backend.domain.safety.entity.EventType;
import naranhi.backend.domain.safety.entity.SafetyEvent;
import naranhi.backend.domain.safety.repository.SafetyEventRepository;
import naranhi.backend.fcm.FcmPayload;
import naranhi.backend.fcm.FcmService;
import naranhi.backend.log.service.MongoLogService;
import naranhi.backend.mqtt.dto.DangerEventMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DangerEventProcessor {

    private final DeviceRepository deviceRepository;
    private final MemberRepository memberRepository;
    private final SafetyEventRepository safetyEventRepository;
    private final NotificationRepository notificationRepository;
    private final SafetyNotificationRepository safetyNotificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final FcmService fcmService;
    private final MongoLogService mongoLogService;

    public void process(DangerEventMessage message) {
        // 1. serial로 장치 조회
        Device device = deviceRepository.findByDeviceSerialNumber(message.deviceSerialNumber())
                .orElseThrow(() -> {
                    log.warn("미등록 장치 - deviceSerialNumber: {}", message.deviceSerialNumber());
                    return new IllegalArgumentException("미등록 장치: " + message.deviceSerialNumber());
                });

        // 2. 연결된 회원 ID 목록 조회
        List<Long> memberIds = deviceRepository.findMemberIdsByDeviceSerialNumber(message.deviceSerialNumber());
        if (memberIds.isEmpty()) {
            log.warn("연결된 회원 없음 - deviceSerialNumber: {}", message.deviceSerialNumber());
            return;
        }

        // 3. EventType 파싱 (MQTT 문자열 → enum)
        EventType eventType;
        try {
            eventType = EventType.valueOf(message.eventType());
        } catch (IllegalArgumentException e) {
            log.error("알 수 없는 EventType - {}", message.eventType());
            return;
        }

        // 4. MySQL 트랜잭션
        Long notifId = saveMysql(message, device, memberIds, eventType);

        // 5. FCM 전송 (MySQL 커밋 후)
        FcmPayload payload = FcmPayload.ofSafety(
                eventType,
                device.getDeviceName(),
                notifId
        );
        fcmService.sendToMembers(memberIds, payload, NotificationType.SAFETY);

        // 6. MongoDB 비동기 로그
        mongoLogService.saveDangerEventLog(message);

        log.info("위험 감지 처리 완료 - deviceSerialNumber: {}, eventType: {}, notifId: {}, 수신자: {}명",
                message.deviceSerialNumber(), eventType, notifId, memberIds.size());
    }

    /**
     * MySQL 트랜잭션 SafetyEvent → Notification → SafetyNotification → NotificationRecipient(N건)
     */
    @Transactional
    public Long saveMysql(
            DangerEventMessage message,
            Device device,
            List<Long> memberIds,
            EventType eventType
    ) {

        // ① SafetyEvent INSERT
        SafetyEvent safetyEvent = safetyEventRepository.save(
                SafetyEvent.builder()
                        .device(device)
                        .eventType(eventType)
                        .severity(eventType.getDefaultSeverity())
                        .confidence(message.confidence() != null
                                ? BigDecimal.valueOf(message.confidence())
                                : null)
                        .durationSecond(message.durationSecond())
                        .snapshotUrl(message.snapshotUrl())
                        .videoUrl(message.videoUrl())
                        .detectedAt(message.detectedAt() != null
                                ? message.detectedAt()
                                : LocalDateTime.now())
                        .build()
        );

        // 장치 마지막 이벤트 시각 업데이트
        device.updateLastEventAt(LocalDateTime.now());
        deviceRepository.save(device);

        // ② Notification INSERT
        Notification notification = notificationRepository.save(
                Notification.builder()
                        .type(NotificationType.SAFETY)
                        .sentAt(LocalDateTime.now())
                        .build()
        );

        // ③ SafetyNotification INSERT
        safetyNotificationRepository.save(
                SafetyNotification.builder()
                        .notification(notification)
                        .safetyEvent(safetyEvent)
                        .device(device)
                        .eventType(eventType)
                        .severity(eventType.getDefaultSeverity())
                        .build()
        );

        // ④ NotificationRecipient INSERT (회원수만큼)
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
