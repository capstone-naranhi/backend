package naranhi.backend.fcm.fcmToken.repository;

import java.util.List;
import java.util.Optional;
import naranhi.backend.fcm.fcmToken.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    // 회원의 활성 토큰 전체 조회 (FCM 전송 시 사용)
    @Query("SELECT f FROM FcmToken f WHERE f.member.id = :memberId AND f.active = true")
    List<FcmToken> findActiveByMemberId(@Param("memberId") Long memberId);

    // 기기 식별자로 토큰 조회 (upsert 시 사용)
    Optional<FcmToken> findByMemberIdAndDeviceId(Long memberId, String deviceId);

    // 토큰 문자열로 직접 조회 (만료 처리 시 사용)
    Optional<FcmToken> findByToken(String token);
}
