package naranhi.backend.mqtt.processor;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        deviceRepository.findByDeviceSerialNumber(message.deviceSerialNumber())
                .ifPresentOrElse(
                        device -> {
                            device.updateHeartbeat(
                                    message.timestamp() != null
                                            ? message.timestamp()
                                            : LocalDateTime.now()
                            );
                            deviceRepository.save(device);
                            log.debug("하트비트 업데이트 - deviceSerialNumber: {}", message.deviceSerialNumber());
                        },
                        () -> saveAsPending(message.deviceSerialNumber())
                );
    }

    private void saveAsPending(String deviceSerialNumber) {
        String key = PENDING_KEY_PREFIX + deviceSerialNumber;
        redisTemplate.opsForValue().set(key, deviceSerialNumber, PENDING_TTL);
        log.info("미등록 장치 Redis 임시 저장 (10분) - deviceSerialNumber: {}", deviceSerialNumber);
    }
}
