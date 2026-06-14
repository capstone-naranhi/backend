package naranhi.backend.domain.safety.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naranhi.backend.domain.safety.dto.DangerState;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DangerStateService {

    private static final String KEY_PREFIX = "device:danger:";
    // START phase: 보드가 다운되더라도 영구적으로 남지 않도록 최대 24시간 TTL
    private static final Duration START_PHASE_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** phase=START 수신 시 호출 */
    public void markStart(String deviceSerial, String eventType, String severity, LocalDateTime detectedAt) {
        DangerState state = new DangerState(eventType, severity, detectedAt, "START", null);
        save(deviceSerial, state, START_PHASE_TTL);
        log.info("[위험상태] START 저장 - serial: {}, eventType: {}", deviceSerial, eventType);
    }

    /** phase=END 수신 시 호출 */
    public void markEnd(String deviceSerial) {
        redisTemplate.delete(KEY_PREFIX + deviceSerial);
        log.info("[위험상태] END 삭제 - serial: {}", deviceSerial);
    }

    /** phase 없이 duration > 0인 이벤트 수신 시 호출 */
    public void markDuration(String deviceSerial, String eventType, String severity,
                             LocalDateTime detectedAt, int durationSeconds) {
        LocalDateTime expiresAt = detectedAt.plusSeconds(durationSeconds);
        long remainingSeconds = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        if (remainingSeconds <= 0) {
            return; // 이미 만료된 이벤트
        }
        DangerState state = new DangerState(eventType, severity, detectedAt, null, expiresAt);
        save(deviceSerial, state, Duration.ofSeconds(remainingSeconds));
        log.info("[위험상태] DURATION 저장 - serial: {}, eventType: {}, 잔여: {}s", deviceSerial, eventType, remainingSeconds);
    }

    /** 현재 위험 상태 조회 (키 없으면 빈 Optional) */
    public Optional<DangerState> getCurrent(String deviceSerial) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + deviceSerial);
        if (json == null) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(json, DangerState.class));
        } catch (JsonProcessingException e) {
            log.error("[위험상태] 역직렬화 실패 - serial: {}", deviceSerial, e);
            return Optional.empty();
        }
    }

    private void save(String deviceSerial, DangerState state, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(KEY_PREFIX + deviceSerial, json, ttl);
        } catch (JsonProcessingException e) {
            log.error("[위험상태] 직렬화 실패 - serial: {}", deviceSerial, e);
        }
    }
}
