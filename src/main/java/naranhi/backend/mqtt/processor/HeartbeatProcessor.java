package naranhi.backend.mqtt.processor;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naranhi.backend.domain.device.repository.DeviceRepository;
import naranhi.backend.mqtt.dto.HeartbeatMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeartbeatProcessor {

    private final DeviceRepository deviceRepository;

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
                        () -> log.warn("미등록 장치 하트비트 - deviceSerialNumber: {}", message.deviceSerialNumber())
                );
    }
}
