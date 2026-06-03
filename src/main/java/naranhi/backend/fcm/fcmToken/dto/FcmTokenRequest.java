package naranhi.backend.fcm.fcmToken.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import naranhi.backend.fcm.fcmToken.entity.PlatformType;

@Getter
public class FcmTokenRequest {

    private final PlatformType platformType = PlatformType.ANDROID;
    @NotBlank(message = "FCM 토큰을 입력해주세요.")
    private String fcmToken;
    @NotBlank(message = "기기 식별자를 입력해주세요.")
    private String deviceId;
}
