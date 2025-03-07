package kr.daeho.AssetAssistant.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kr.daeho.AssetAssistant.auth.interfaces.AuthInterfaces;
import kr.daeho.AssetAssistant.auth.dto.TokenResponseDto;
import kr.daeho.AssetAssistant.auth.dto.LoginRequestDto;
import kr.daeho.AssetAssistant.common.controller.BaseController;
import kr.daeho.AssetAssistant.common.dto.ApiResponse;

/**
 * 사용자 로그인 컨트롤러
 * 
 * 애플리케이션의 진입점 역할로, 클라이언트의 요청을 받아 서비스 계층으로 전달하고, 결과를 클라이언트에 반환
 * 
 * 컨트롤러는 클라이언트의 요청을 받아 서비스 계층으로 전달하고, 결과를 클라이언트에 반환
 * 
 * ResponseEntity: HTTP 상태 코드(예: 200 OK, 204 No Content, 404 Not Found 등)를 함께 반환
 * 
 * 생성자를 통한 의존성 주입 -> 의존성 역전 원칙 (불변성 보장, 필수 의존성 보장, 테스트 용이)
 * 
 * 고수준 모듈(컨트롤러)이 저수준 모듈(서비스)에 직접 의존하지 않음 (인터페이스(계약)에 의존)
 * 
 * @RestController: 컨트롤러 클래스임을 명시
 * @RequestMapping: 사용자 관련 요청 URL과 매핑하기 위한 기본 프리픽스(~/user/~)
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController extends BaseController {
    /**
     * 사용자 로그인 인터페이스 주입
     * 
     * 컨트롤러는 서비스(실제)가 아닌 인터페이스(계약)에 의존하여 의존성 역전 및 느슨한 결합 확보
     */
    private final AuthInterfaces authInterfaces;

    /**
     * 로그인 처리 -> 클라이언트의 로그인 요청에 대해 토큰 응답을 생성하여 반환
     * 
     * TokenResponseDto: 로그인 성공 시, 발급된 토큰 정보
     * LoginRequestDto: 로그인 요청 시, 클라이언트가 제공하는 사용자 아이디, 비밀번호 등 인증 정보
     * 
     * @RequestBody: HTTP 요청의 본문(JSON 등)을 LoginRequestDto 객체로 자동 변환
     * @Valid: 변환된 객체에 대해 유효성 검사를 수행
     * 
     * @param loginRequest 로그인 요청 정보
     * @return 로그인 성공 시 200 OK 응답
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(
            @Valid @RequestBody LoginRequestDto loginRequest) {
        log.info("로그인 요청: {}", loginRequest.getUserId());

        // 로그인에 성공한 경우, TokenResponseDto 객체(JWT 액세스 토큰, 리프레시 토큰 등)를 반환
        TokenResponseDto tokenResponse = authInterfaces.login(loginRequest);

        return success(tokenResponse, "로그인 성공");
    }
}
