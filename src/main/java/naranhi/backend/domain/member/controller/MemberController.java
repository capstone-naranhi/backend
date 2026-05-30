package naranhi.backend.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import naranhi.backend.auth.LoginUser;
import naranhi.backend.auth.SessionUser;
import naranhi.backend.domain.member.dto.MemberResponse;
import naranhi.backend.domain.member.dto.SignupRequest;
import naranhi.backend.domain.member.service.MemberService;
import naranhi.backend.global.exception.CustomException;
import naranhi.backend.global.exception.ErrorCode;
import naranhi.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입 POST /api/v1/auth/signup
     */
    @PostMapping("/signup")
    public ApiResponse<MemberResponse.Signup> signup(@RequestBody @Valid SignupRequest request) {
        return ApiResponse.created(memberService.signup(request));
    }

    /**
     * 이메일 중복 확인 GET /api/v1/auth/check-email?email=xxx
     */
    @GetMapping("/check-email")
    public ApiResponse<Void> checkEmail(@RequestParam String email) {
        if (!memberService.isEmailAvailable(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        return ApiResponse.ok(null);
    }

    /**
     * 내 정보 조회 GET /api/v1/auth/me
     */
    @GetMapping("/me")
    public ApiResponse<MemberResponse.MyInfo> getMyInfo(@LoginUser SessionUser loginUser) {
        return ApiResponse.ok(memberService.getMyInfo(loginUser.getId()));
    }

    /**
     * 세션 만료 응답 GET /api/v1/auth/invalid-session
     */
    @GetMapping("/invalid-session")
    public ApiResponse<Void> invalidSession() {
        throw new CustomException(ErrorCode.SESSION_EXPIRED);
    }

    // POST /api/v1/auth/login  → Spring Security 자동 처리 (컨트롤러 불필요)
    // POST /api/v1/auth/logout → Spring Security 자동 처리 (컨트롤러 불필요)
}