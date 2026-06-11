package naranhi.backend.live.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import naranhi.backend.domain.device.entity.Device;
import naranhi.backend.domain.device.repository.DeviceRepository;
import naranhi.backend.domain.device.repository.MemberDeviceRepository;
import naranhi.backend.global.exception.CustomException;
import naranhi.backend.global.exception.ErrorCode;
import naranhi.backend.live.dto.LiveResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LiveService {

    private final DeviceRepository deviceRepository;
    private final MemberDeviceRepository memberDeviceRepository;

    public LiveResponse.Session createSession(Long memberId, Long deviceId) {
        if (!memberDeviceRepository.existsByMemberIdAndDeviceId(memberId, deviceId)) {
            throw new CustomException(ErrorCode.DEVICE_ACCESS_DENIED);
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

        String sessionId = UUID.randomUUID().toString();

        return new LiveResponse.Session(sessionId, device.getDeviceSerialNumber());
    }
}
