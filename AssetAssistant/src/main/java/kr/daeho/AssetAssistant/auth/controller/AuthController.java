package kr.daeho.AssetAssistant.auth.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;

import kr.daeho.AssetAssistant.auth.interfaces.AuthInterfaces;
import kr.daeho.AssetAssistant.auth.dto.LoginRequestDto;
import kr.daeho.AssetAssistant.common.controller.BaseController;
import kr.daeho.AssetAssistant.common.utils.CookieUtil;

/**
 * 사용자 인증 컨트롤러
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
     * 사용자 인증 인터페이스 주입
     * 
     * 컨트롤러는 서비스(실제)가 아닌 인터페이스(계약)에 의존하여 의존성 역전 및 느슨한 결합 확보
     */
    private final AuthInterfaces authInterfaces;

    // 쿠키 설정 상수
    private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7일
    private static final String ACCESS_TOKEN_COOKIE = "accessToken"; // 액세스 토큰 쿠키 이름
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken"; // 리프레시 토큰 쿠키 이름

    /**
     * 로그인 처리 -> 클라이언트의 로그인 요청에 대해 토큰 응답을 생성하여 반환
     * 
     * LoginRequestDto: 로그인 요청 시, 클라이언트가 제공하는 사용자 아이디, 비밀번호 등 인증 정보
     * 
     * HttpServletResponse: HTTP 응답 메시지 생성
     * - HTTP 응답 메시지 생성
     * - 응답 코드 지정 (200, 300, 400, 500 등)
     * - 바디 생성 가능
     * - 여러 편의 기능 제공 (Content-type, Cookie, Redirect)
     * 
     * Map<String, Object>: 로그인 성공 시 토큰 정보 (사용자 ID, 액세스 토큰, 리프레시 토큰)
     * 
     * @Validated 변환된 객체에 대해 유효성 검사를 수행
     * @RequestBody: HTTP 요청의 본문(JSON 등)을 LoginRequestDto 객체로 자동 변환
     * 
     * @param loginRequest 로그인 요청 정보
     * @param response     HTTP 응답 메시지 생성 객체
     * @return 로그인 성공 시 200 OK 응답
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Validated @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response) {
        log.info("로그인 요청: {}", loginRequestDto.getUserId());

        // 로그인 처리 -> 로그인 성공 시 토큰 정보 (사용자 ID, 액세스 토큰, 리프레시 토큰)
        Map<String, Object> loginResult = authInterfaces.login(loginRequestDto);

        // 액세스 토큰과 리프레시 토큰 추출
        String accessToken = (String) loginResult.get("accessToken");
        String refreshToken = (String) loginResult.get("refreshToken");

        // 액세스 토큰은 일반 쿠키에 저장
        CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE, accessToken, (int) (60 * 30)); // 30분

        // 리프레시 토큰은 보안 쿠키에 저장 (HttpOnly, Secure, SameSite 설정)
        CookieUtil.addSecureCookie(response, REFRESH_TOKEN_COOKIE, refreshToken, (int) (60 * 60 * 24 * 7)); // 7일

        // 쿠키에 저장했으므로 응답에서는 제외
        loginResult.remove("accessToken");
        loginResult.remove("refreshToken");

        return ResponseEntity.ok(loginResult);
    }

    /**
     * 토큰 갱신
     * 
     * HttpServletResponse: HTTP 응답 메시지 생성
     * - HTTP 응답 메시지 생성
     * - 응답 코드 지정 (200, 300, 400, 500 등)
     * - 바디 생성 가능
     * - 여러 편의 기능 제공 (Content-type, Cookie, Redirect)
     * 
     * @CookieValue: 쿠키 값 추출
     * @param refreshToken 리프레시 토큰
     * @param response     HTTP 응답 메시지 생성 객체
     * @return 토큰 갱신 성공 시 200 OK 응답
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(401).body("리프레시 토큰이 없습니다.");
        }

        try {
            // 토큰 갱신
            Map<String, Object> tokenInfo = authInterfaces.refreshToken(refreshToken);

            // 새 액세스 토큰 추출
            String accessToken = (String) tokenInfo.get("accessToken");

            // 새 액세스 토큰 쿠키 설정
            Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE, accessToken); // 엑세스 쿠키 이름, 액세스 토큰
            accessTokenCookie.setHttpOnly(true); // 쿠키 보안 설정 (HttpOnly)
            accessTokenCookie.setSecure(true); // 쿠키 보안 설정 (Secure)
            accessTokenCookie.setPath("/"); // 쿠키 경로
            accessTokenCookie.setMaxAge(COOKIE_MAX_AGE); // 쿠키 만료 시간
            response.addCookie(accessTokenCookie); // 응답에 쿠키 추가

            log.info("토큰 갱신 성공");

            return ResponseEntity.ok().body("토큰이 갱신되었습니다.");
        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.status(401).body("토큰 갱신에 실패했습니다.");
        }
    }

    /**
     * 로그아웃
     * 
     * HttpServletRequest: HTTP 요청 메시지
     * - 파라미터, 헤더, 쿠키 및 세션 정보 등 세부 정보 캡슐화
     * - 클라이언트 요청 발생 시, 요청에 포함된 데이터 가져오기 가능
     * 
     * HttpServletResponse: HTTP 응답 메시지 생성
     * - HTTP 응답 메시지 생성
     * - 응답 코드 지정 (200, 300, 400, 500 등)
     * - 바디 생성 가능
     * - 여러 편의 기능 제공 (Content-type, Cookie, Redirect)
     * 
     * @CookieValue: 쿠키 값 추출
     * @RequestParam: 쿠키에서 추출할 디바이스 ID
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 메시지 생성 객체
     * @param deviceId 디바이스 ID (name: "deviceId"라는 이름의 파라미터를 찾아서 할당, 필수 아님)
     * @return 로그아웃 성공 시 204 No Content 응답
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(name = "deviceId", required = false) String deviceId) {

        // 클라이언트 요청에서 쿠키 가져오기
        Cookie[] cookies = request.getCookies();
        // 쿠키에서 액세스 토큰 가져오기
        String accessToken = CookieUtil.getCookieValue(cookies, ACCESS_TOKEN_COOKIE);

        if (accessToken != null) {
            // 토큰에서 사용자 ID 추출
            String userId = authInterfaces.validateToken(accessToken);

            // 디바이스 ID가 있으면 해당 디바이스만 로그아웃, 없으면 모든 디바이스 로그아웃
            if (deviceId != null && !deviceId.isEmpty()) {
                authInterfaces.logout(userId, deviceId);
                log.info("사용자 {} 디바이스 {} 로그아웃 처리", userId, deviceId);
            } else {
                authInterfaces.logout(userId);
                log.info("사용자 {} 모든 디바이스 로그아웃 처리", userId);
            }

            // 쿠키 삭제
            CookieUtil.deleteCookie(response, ACCESS_TOKEN_COOKIE);
            CookieUtil.deleteCookie(response, REFRESH_TOKEN_COOKIE);
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
