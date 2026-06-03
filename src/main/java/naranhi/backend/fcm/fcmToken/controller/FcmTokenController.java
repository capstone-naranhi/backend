package naranhi.backend.fcm.fcmToken.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import naranhi.backend.auth.LoginUser;
import naranhi.backend.auth.SessionUser;
import naranhi.backend.fcm.fcmToken.dto.FcmTokenRequest;
import naranhi.backend.fcm.fcmToken.service.FcmTokenService;
import naranhi.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    /**
     * FCM 토큰 등록·갱신 PUT /api/v1/fcm/token 앱 실행 시마다 호출
     */
    @PutMapping("/token")
    public ApiResponse<Void> registerToken(
            @LoginUser SessionUser loginUser,
            @RequestBody @Valid FcmTokenRequest request
    ) {
        fcmTokenService.registerOrUpdate(loginUser.getId(), request);
        return ApiResponse.ok(null);
    }
}