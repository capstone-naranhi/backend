package naranhi.backend.fcm.fcmToken.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import naranhi.backend.domain.member.entity.Member;
import naranhi.backend.global.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "fcm_token")
public class FcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "token", nullable = false, length = 500)
    private String token;

    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type", nullable = false, length = 10)
    private PlatformType platformType;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    public static FcmToken create(
            Member member,
            String token,
            String deviceId,
            PlatformType platform
    ) {
        return FcmToken.builder()
                .member(member)
                .token(token)
                .deviceId(deviceId)
                .platformType(platform)
                .active(true)
                .lastUsedAt(LocalDateTime.now())
                .build();
    }

    public void updateToken(String newToken) {
        this.token = newToken;
        this.lastUsedAt = LocalDateTime.now();
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

}
