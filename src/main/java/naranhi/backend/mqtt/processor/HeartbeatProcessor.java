package naranhi.backend.mqtt.processor;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naranhi.backend.domain.device.entity.DeviceStatus;
import naranhi.backend.domain.device.repository.DeviceRepository;
import naranhi.backend.mqtt.dto.HeartbeatMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeartbeatProcessor {

    public static final String PENDING_KEY_PREFIX = "pending_device:";
    private static final Duration PENDING_TTL = Duration.ofMinutes(10);

    private final DeviceRepository deviceRepository;
    private final StringRedisTemplate redisTemplate;

    public void process(HeartbeatMessage message) {
        updateMysql(message);
    }

    @Transactional
    public void updateMysql(HeartbeatMessage message) {
        deviceRepository.findByDeviceSerialNumber(message.deviceSerial())
                .ifPresentOrElse(
                        device -> {
                            LocalDateTime heartbeatAt = message.timestamp() != null
                                    ? message.timestamp().toLocalDateTime()
                                    : LocalDateTime.now();
                            DeviceStatus cameraStatus = "ONLINE".equals(message.cameraStatus())
                                    ? DeviceStatus.ONLINE : DeviceStatus.OFFLINE;
                            DeviceStatus micStatus = "ONLINE".equals(message.micStatus())
                                    ? DeviceStatus.ONLINE : DeviceStatus.OFFLINE;
                            device.updateHeartbeat(heartbeatAt, cameraStatus, micStatus,
                                    message.cpuUsage(), message.memoryUsage());
                            deviceRepository.save(device);
                            log.debug("하트비트 업데이트 - serial: {}, cpu: {}%, mem: {}%",
                                    message.deviceSerial(), message.cpuUsage(), message.memoryUsage());
                        },
                        () -> saveAsPending(message.deviceSerial())
                );
    }

    private void saveAsPending(String deviceSerial) {
        String key = PENDING_KEY_PREFIX + deviceSerial;
        redisTemplate.opsForValue().set(key, deviceSerial, PENDING_TTL);
        log.info("미등록 장치 Redis 임시 저장 (10분) - serial: {}", deviceSerial);
    }
}
