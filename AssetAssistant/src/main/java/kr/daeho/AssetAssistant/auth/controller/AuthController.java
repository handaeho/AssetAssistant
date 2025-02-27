package kr.daeho.AssetAssistant.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kr.daeho.AssetAssistant.auth.dto.LoginRequestDto;
import kr.daeho.AssetAssistant.auth.dto.TokenResponseDto;
import kr.daeho.AssetAssistant.auth.service.AuthService;
import kr.daeho.AssetAssistant.common.controller.BaseController;
import kr.daeho.AssetAssistant.common.dto.ApiResponse;

/**
 * 인증 관련 요청을 처리하는 REST 컨트롤러
 * 
 * 로그인 등의 인증 관련 엔드포인트를 제공
 * 
 * [인증 과정 1-2단계]: 사용자 아이디/비밀번호 입력 -> HTTP 요청 처리
 * 
 * @RestController: 컨트롤러 클래스임을 명시
 * @RequestMapping: 사용자 인증 관련 요청 URL과 매핑하기 위한 기본 프리픽스(~/auth/~)
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController extends BaseController {
    // final로 선언해 불변성 보장, @RequiredArgsConstructor로 생성자 자동 생성 및 의존성 주입
    private final AuthService authService; // 인증 관련 서비스

    /**
     * 로그인 요청 처리 -> 사용자 인증 및 JWT 토큰 발급
     * 
     * [인증 과정 1-2단계]: 사용자 아이디 패스워드 입력 -> 서버로 전달
     * 
     * POST /api/auth/login
     * 
     * @param loginRequestDto 로그인 요청 정보 (@Valid: 유효성 검증)
     * @return ResponseEntity<ApiResponse<TokenResponseDto>> JWT 토큰 포함 응답
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("로그인 요청: {}", loginRequestDto.getUserId());

        // [인증 과정 3-16단계]: 로그인 처리 및 JWT 토큰 발급 (예외 발생 시 GlobalExceptionHandler로 전파)
        // loginRequestDto로 받은 아이디, 비밀번호 검증 후, JWT 토큰 발급
        String token = authService.login(loginRequestDto);

        // [인증 과정 14단계]: 클라이언트에게 JWT 토큰 전달 (DTO 객체 사용)
        TokenResponseDto response = TokenResponseDto.builder()
                .token(token)
                .userId(loginRequestDto.getUserId())
                .build();

        return success(response, "로그인이 완료되었습니다");
    }

    /**
     * 토큰 유효성 검증
     * 
     * @param token 검증할 토큰
     * @return 토큰 유효성 검증 결과
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestParam String token) {
        log.info("토큰 유효성 검증 요청");

        // 토큰 검증 (예외 발생 시 GlobalExceptionHandler로 전파)
        boolean isValid = authService.validateToken(token);

        return success(isValid, isValid ? "유효한 토큰입니다" : "유효하지 않은 토큰입니다");
    }
}
