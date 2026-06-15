package naranhi.backend.config;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import naranhi.backend.auth.CustomUserDetails;
import naranhi.backend.auth.CustomUserDetailsService;
import naranhi.backend.auth.SessionUser;
import naranhi.backend.global.exception.ErrorCode;
import naranhi.backend.global.response.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/api/v1/auth/invalid-session")
                        .maximumSessions(5)
                        .maxSessionsPreventsLogin(false)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/signup",
                                "/api/v1/auth/check-email",
                                "/api/v1/auth/invalid-session",
                                // S3 presigned URL (Jetson 보드 인증 없이 사용)
                                "/api/v1/s3/presigned/**",
                                // Swagger
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginProcessingUrl("/api/v1/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler())
                        .failureHandler(loginFailureHandler())
                )

                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(logoutSuccessHandler())
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─── 핸들러 ──────────────────────────────────────────────────────

    /**
     * 로그인 성공 세션에 loginUser 저장 후 ApiResponse.ok() 반환
     */
    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            SessionUser sessionUser = new SessionUser(
                    userDetails.getMemberId(),
                    userDetails.getName(),
                    userDetails.getNickname(),
                    userDetails.getEmail(),
                    userDetails.getRole()
            );

            HttpSession session = request.getSession();
            session.setAttribute("loginUser", sessionUser);
            session.setMaxInactiveInterval(60 * 60 * 24 * 7);

            writeResponse(response, HttpStatus.OK, ApiResponse.ok(sessionUser));
        };
    }

    /**
     * 로그인 실패 BadCredentialsException → LOGIN_FAILED 에러 반환
     */
    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            ErrorCode errorCode = (exception instanceof BadCredentialsException)
                    ? ErrorCode.LOGIN_FAILED
                    : ErrorCode.UNAUTHORIZED;
            writeResponse(response, errorCode.getHttpStatus(), ApiResponse.fail(errorCode));
        };
    }

    /**
     * 로그아웃 성공
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) ->
                writeResponse(response, HttpStatus.OK, ApiResponse.ok(null));
    }

    /**
     * 401: 인증 안 된 요청 세션 없이 인증이 필요한 API 접근 시
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) ->
                writeResponse(response, HttpStatus.UNAUTHORIZED, ApiResponse.fail(ErrorCode.UNAUTHORIZED));
    }

    /**
     * 403: 권한 없는 요청
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                writeResponse(response, HttpStatus.FORBIDDEN, ApiResponse.fail(ErrorCode.FORBIDDEN));
    }

    // ─── 헬퍼 ────────────────────────────────────────────────────────

    /**
     * Security 핸들러에서 ApiResponse를 JSON으로 직접 작성 ResponseHttpStatusSetterAdvice는 Spring MVC 영역에서만 동작하므로 Security 필터 체인에서는
     * 이 메서드로 상태코드 + body 직접 설정
     */
    private void writeResponse(
            jakarta.servlet.http.HttpServletResponse response,
            HttpStatus status,
            ApiResponse<?> apiResponse
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}