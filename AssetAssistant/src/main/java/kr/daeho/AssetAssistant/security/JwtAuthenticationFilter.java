package kr.daeho.AssetAssistant.security;

import java.io.IOException;
import java.util.ArrayList;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import kr.daeho.AssetAssistant.auth.interfaces.AuthInterfaces;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;

/**
 * JWT 토큰 인증 필터
 * 
 * 모든 HTTP 요청에 대해 JWT 토큰을 확인하고 유효한 경우 사용자 인증을 수행
 * 
 * Spring Security 필터 체인에 등록되어 실행
 * -> HTTP 요청이 들어오면 여러 보안 관련 처리를 수행하는 일련의 필터 집합
 * -> 각 필터가 순차적으로 요청을 가로채고, 인증, 인가, 로깅, CSRF 보호 등 다양한 작업을 수행
 * 
 * OncePerRequestFilter를 상속받아 구현
 * OncePerRequestFilter: 모든 HTTP 요청에 대해 한 번만 실행되는 필터
 * 
 * OncePerRequestFilter를 상속한 클래스에서 doFilterInternal을 재정의하면,
 * 요청이 필터 체인을 통과할 때 스프링이 자동으로 이 메서드를 실행
 * 
 * @Component: 스프링 컨테이너에 빈으로 등록
 *             - Bean: 스프링 컨테이너에 의해 관리되는 객체
 *             - 범용적인 빈 등록을 위한 어노테이션. 특별한 프록시 처리는 하지 않음
 *             - 설정 관련 빈 등록 및 프록시 설정을 위한 @Configuration 컴포넌트보다 범용적으로 사용하는 용도
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션 (@Autowired 대신 사용)
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    /**
     * 인증 서비스 인터페이스 주입
     */
    private final AuthInterfaces authInterfaces;

    /**
     * 액세스 토큰 쿠키 이름 선언
     */
    private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";

    /**
     * 필터 로직 구현
     * 
     * 모든 HTTP 요청에 대해 JWT 토큰을 확인하고 유효한 경우 인증 처리
     * 
     * HTTP 요청을 처리하는 핵심 메서드로, 필터의 구체적인 동작을 정의
     * 
     * @NonNull: 메서드 매개변수가 null이 되지 않도록 보장
     * 
     * @param request     HTTP 요청 객체
     * @param response    HTTP 응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외 (필터 처리 중 발생 가능한 예외)
     * @throws IOException      입출력 예외 (필터 처리 중 발생 가능한 예외)
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 쿠키에서 액세스 토큰 추출
        String accessToken = extractTokenFromCookie(request, ACCESS_TOKEN_COOKIE);

        if (accessToken != null) {
            try {
                // 액세스 토큰 검증
                String userId = authInterfaces.validateToken(accessToken);

                // 인증 객체 생성
                Authentication auth = createAuthenticationToken(userId);
                // 인증 정보 저장 (SecurityContextHolder: 애플리케이션 전체에서 현재 사용자의 인증 정보를 참조 가능)
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("사용자 {} 인증 완료", userId);
            } catch (ApplicationException.InvalidTokenException e) {
                log.error("토큰 검증 실패: {}", e.getMessage());
                // 인증 실패 시에도 필터 체인은 계속 진행
            } catch (Exception e) {
                log.error("인증 처리 중 오류 발생: {}", e.getMessage());
            }
        }

        // 현재 필터의 처리가 끝난 후, 요청과 응답을 필터 체인의 다음 필터로 전달
        filterChain.doFilter(request, response);
    }

    /**
     * 쿠키에서 토큰 추출
     * 
     * HttpServletRequest: HTTP 요청 메시지
     * - 파라미터, 헤더, 쿠키 및 세션 정보 등 세부 정보 캡슐화
     * - 클라이언트 요청 발생 시, 요청에 포함된 데이터 가져오기 가능
     * 
     * @param request    HTTP 요청 객체
     * @param cookieName 쿠키 이름
     * @return 쿠키 값
     */
    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 인증 객체 생성
     * 
     * UsernamePasswordAuthenticationToken: Authentication 인터페이스를 구현, 사용자 인증정보 저장
     * 
     * @param userId 사용자 ID
     * @return 인증 객체
     */
    private Authentication createAuthenticationToken(String userId) {
        // NOTE: 간단한 인증 객체 생성 (추후 필요시 UserDetails로 확장 가능)
        return new UsernamePasswordAuthenticationToken(
                userId, null, new ArrayList<>());
    }

    /**
     * 인증이 필요없는 URL 패턴 검사
     * 
     * @param request HTTP 요청 객체
     * @return 인증이 필요없는 경우 true, 인증이 필요한 경우 false
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // 인증이 필요없는 URL 패턴
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/users/signup") ||
                path.startsWith("/api/users/check-id-duplicate");
    }
}
