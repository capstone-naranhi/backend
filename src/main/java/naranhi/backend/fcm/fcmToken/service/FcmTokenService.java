package naranhi.backend.fcm.fcmToken.service;


import java.util.Optional;
import lombok.RequiredArgsConstructor;
import naranhi.backend.domain.member.entity.Member;
import naranhi.backend.domain.member.repository.MemberRepository;
import naranhi.backend.fcm.fcmToken.dto.FcmTokenRequest;
import naranhi.backend.fcm.fcmToken.entity.FcmToken;
import naranhi.backend.fcm.fcmToken.repository.FcmTokenRepository;
import naranhi.backend.global.exception.CustomException;
import naranhi.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;

    /**
     * FCM 토큰 등록 or 갱신 (Upsert) 앱 실행 시마다 호출 → 항상 최신 토큰 유지
     */
    public void registerOrUpdate(Long memberId, FcmTokenRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Optional<FcmToken> existing = fcmTokenRepository
                .findByMemberIdAndDeviceId(memberId, request.getDeviceId());

        if (existing.isPresent()) {
            // 이미 등록된 기기 → 토큰만 갱신
            existing.get().updateToken(request.getFcmToken());
        } else {
            // 새 기기 → 신규 등록
            fcmTokenRepository.save(
                    FcmToken.create(
                            member,
                            request.getFcmToken(),
                            request.getDeviceId(),
                            request.getPlatformType()
                    )
            );
        }
    }

    /**
     * 토큰 만료 처리 FCM 전송 시 UNREGISTERED 에러 받으면 호출
     */
    public void deactivateToken(String token) {
        fcmTokenRepository.findByToken(token)
                .ifPresent(FcmToken::deactivate);
    }
}