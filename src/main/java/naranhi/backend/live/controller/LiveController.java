package naranhi.backend.live.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import naranhi.backend.auth.LoginUser;
import naranhi.backend.auth.SessionUser;
import naranhi.backend.global.response.ApiResponse;
import naranhi.backend.live.dto.LiveRequest;
import naranhi.backend.live.dto.LiveResponse;
import naranhi.backend.live.service.LiveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/live")
@RequiredArgsConstructor
public class LiveController {

    private final LiveService liveService;

    @PostMapping("/session")
    public ApiResponse<LiveResponse.Session> createSession(
            @LoginUser SessionUser loginUser,
            @Valid @RequestBody LiveRequest.CreateSession request
    ) {
        return ApiResponse.ok(liveService.createSession(loginUser.getId(), request.deviceId()));
    }
}
