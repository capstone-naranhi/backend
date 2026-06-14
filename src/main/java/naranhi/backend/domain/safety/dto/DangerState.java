package naranhi.backend.domain.safety.dto;

import java.time.LocalDateTime;

/**
 * 현재 진행 중인 위험 상태 (Redis 저장용)
 * - phase=START : END가 올 때까지 지속
 * - phase=null  : expiresAt까지만 지속 (duration 기반)
 */
public record DangerState(
        String eventType,
        String severity,
        LocalDateTime detectedAt,
        String phase,          // "START" | null
        LocalDateTime expiresAt  // phase=START이면 null
) {}
