package kr.daeho.AssetAssistant.auth.controller;

import java.util.Map;
import java.util.HashMap;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import kr.daeho.AssetAssistant.auth.service.AuthService;
import kr.daeho.AssetAssistant.auth.dto.SignUpRequestDto;
import kr.daeho.AssetAssistant.auth.dto.LoginRequestDto;

/**
 * 인증 관련 요청을 처리하는 REST 컨트롤러
 * 
 * 회원가입, 로그인 등의 인증 관련 엔드포인트를 제공
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
public class AuthController {
    // 인증 서비스
    // // final로 선언해 불변성 보장, @RequiredArgsConstructor로 생성자 자동 생성 및 의존성 주입
    private final AuthService authService;

    /**
     * 회원가입 요청 처리
     * 
     * POST /api/auth/signup
     * 
     * @param signUpRequestDto 회원가입 요청 정보 (@Valid: 유효성 검증)
     * @return ResponseEntity<Map<String, String>> 처리 결과
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        log.info("회원가입 요청: {}", signUpRequestDto.getUserId());

        // 회원가입 처리
        authService.signUp(signUpRequestDto);

        // 응답 데이터 구성
        Map<String, String> response = new HashMap<>();
        response.put("message", "회원가입이 성공적으로 완료되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 요청 처리
     * 
     * [인증 과정 1-2단계]: 사용자 아이디 패스워드 입력 -> 서버로 전달
     * 
     * POST /api/auth/login
     * 
     * @param loginRequestDto 로그인 요청 정보 (@Valid: 유효성 검증)
     * @return ResponseEntity<Map<String, String>> JWT 토큰 포함 응답
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("로그인 요청: {}", loginRequestDto.getUserId());

        // [인증 과정 3-16단계]: 로그인 처리 및 JWT 토큰 발급
        String token = authService.login(loginRequestDto);

        // [인증 과정 14단계]: 클라이언트에게 JWT 토큰 전달
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", loginRequestDto.getUserId());

        return ResponseEntity.ok(response);
    }
}
