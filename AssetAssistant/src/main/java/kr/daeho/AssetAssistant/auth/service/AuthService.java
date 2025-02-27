package kr.daeho.AssetAssistant.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import kr.daeho.AssetAssistant.auth.dto.LoginRequestDto;
import kr.daeho.AssetAssistant.common.exception.ApplicationExceptions;
import kr.daeho.AssetAssistant.security.JWTokenProvider;

/**
 * 인증 관련 비즈니스 로직 처리 서비스 -> 인증 및 인가 관련 비즈니스 로직 처리 담당
 * 
 * 회원가입, 로그인 등 인증 관련 기능을 제공
 * 
 * 컨트롤러에서 요청을 받아 비즈니스 로직을 처리하고, 결과를 반환
 * 
 * 핵심 비즈니스 로직 처리, 트랜잭션 관리, 예외 처리 등 웹 요청 및 응답을 위한 실제 로직 처리
 * 
 * 인터페이스를 상속받아 서비스를 구현(implements)함으로써,
 * 
 * 컨트롤러는 서비스(실제 구현체)가 아닌 인터페이스(계약)에 의존하여 의존성 역전 및 느슨한 결합 확보
 * 
 * [인증 과정]의 3 ~ 5번 단계, 12 ~ 13번 단계
 * 
 * 3. AuthenticationFilter에서 유효성 검사 & UsernamePasswordAuthenticationToken 객체 생성
 * -> UsernamePasswordAuthenticationToken: Authentication 인터페이스를 구현, 사용자 인증정보 저장
 * -> 사용자의 아이디와 비밀번호를 추출하여 UsernamePasswordAuthenticationToken 객체를 생성
 * -> JWT 기반 인증 적용 시, 커스텀 필터나 커스텀 성공 핸들러를 사용해 토큰을 생성
 * 
 * 4. AuthenticationManager에 UsernamePasswordAuthenticationToken 전달. 인증 처리 시작
 * -> AuthenticationManager: 인증 처리 과정을 담당하는 핵심 컴포넌트
 * -> 여러 AuthenticationProvider를 순차적으로 호출할 수도 있음
 * 
 * 5. AuthenticationProvider에게 UsernamePasswordAuthenticationToken 전달
 * -> AuthenticationProvider: 실제 인증 로직 처리 (기본적으로는 DaoAuthenticationProvider 사용)
 * 
 * 12. 성공 시, AuthenticationSuccessHandler 핸들러 실행
 * -> 사용자를 홈 화면이나 요청했던 페이지로 리디렉션 등
 * 
 * 13. AuthenticationSuccessHandler에서 JWT 발급 수행
 * -> 인증 성공 후, 커스텀 AuthenticationSuccessHandler (또는 필터 내 로직)에서
 * -> 사용자 정보(예: 아이디, 권한 등)를 기반으로 JWT를 생성
 * -> 생성된 JWT 토큰을 HTTP 응답 헤더(예: Authorization)나 응답 본문에 포함하여 클라이언트에게 전달
 * 
 * @Service: 서비스 클래스임을 명시
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션 (@Autowired 대신 사용)
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    // final로 선언해 불변성 보장, @RequiredArgsConstructor로 생성자 자동 생성 및 의존성 주입
    private final JWTokenProvider jwtTokenProvider; // JWT 토큰 발급, 검증 등
    private final AuthenticationManager authenticationManager; // 인증 처리 과정을 담당하는 핵심 컴포넌트

    /**
     * 로그인 처리
     * 사용자 인증을 수행하고 JWT 토큰을 발급
     * 
     * [인증 과정 3-5단계]:
     * AuthenticationFilter -> AuthenticationManager -> AuthenticationProvider
     * 
     * [인증 과정 12-13단계]: 인증 성공 -> JWT 토큰 생성
     * 
     * [처리 절차]
     * 
     * 1. 인증 정보 생성
     * 
     * 2. 인증 매니저를 통한 인증 처리
     * 
     * 3. JWT 토큰 생성
     * 
     * @param loginRequestDto 로그인 요청 정보
     * @return 생성된 JWT 토큰
     * @throws AuthenticationFailedException 인증 실패 시
     */
    public String login(LoginRequestDto loginRequestDto) {
        try {
            log.info("로그인 요청 처리: {}", loginRequestDto.getUserId());

            // 1. 인증 객체 생성 및 인증 처리
            // 인증 정보 생성 -> Authentication 인터페이스를 구현, 사용자 인증정보 저장
            // loginRequestDto에 있는 사용자의 아이디와 비밀번호를 통해 인증 토큰 객체 생성
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUserId(),
                            loginRequestDto.getPassword()));

            log.debug("인증 성공: {}", loginRequestDto.getUserId());

            // 2. 인증 성공 시(인증 토큰 객체 생성 성공 시), 해당 정보로 JWT 토큰 생성 및 반환
            String token = jwtTokenProvider.generateToken(authentication);

            log.info("JWT 토큰 생성 완료: {}", loginRequestDto.getUserId());

            return token;
        } catch (AuthenticationException e) {
            // 스프링 시큐리티 예외를 애플리케이션 예외로 변환하여 일관된 처리
            log.error("로그인 실패: {}", e.getMessage());
            throw new ApplicationExceptions.AuthenticationFailedException(e);
        }
    }

    /**
     * 주어진 토큰이 유효한지 검증
     * 
     * @param token 검증할 JWT 토큰
     * @return 토큰 유효성 여부
     * @throws ApplicationExceptions.InvalidTokenException 토큰이 유효하지 않을 때
     */
    public boolean validateToken(String token) {
        // 토큰이 null이거나 비어있는지 검사
        if (token == null || token.isEmpty()) {
            throw new ApplicationExceptions.InvalidTokenException("토큰이 제공되지 않았습니다");
        }

        return jwtTokenProvider.validateToken(token);
    }

    /**
     * 토큰에서 사용자 ID 추출
     * 
     * @param token JWT 토큰
     * @return 사용자 ID
     * @throws ApplicationExceptions.InvalidTokenException 토큰이 유효하지 않을 때
     */
    public String getUserIdFromToken(String token) {
        // 토큰이 null이거나 비어있는지 검사
        if (token == null || token.isEmpty()) {
            throw new ApplicationExceptions.InvalidTokenException("토큰이 제공되지 않았습니다");
        }

        return jwtTokenProvider.getUserIdFromToken(token);
    }
}
