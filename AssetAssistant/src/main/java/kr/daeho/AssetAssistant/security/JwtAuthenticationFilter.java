package kr.daeho.AssetAssistant.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import kr.daeho.AssetAssistant.common.exception.ApplicationException;

/**
 * JWT 인증 필터
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
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션 (@Autowired 대신 사용)
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    /**
     * JWT 토큰 처리를 위한 컴포넌트
     */
    private final JWTokenProvider tokenProvider;

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

        try {
            // 요청 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);
            log.debug("JWT 토큰 추출: {}", jwt != null ? "성공" : "실패");

            // 토큰이 유효할 경우 인증 정보 설정
            // StringUtils.hasText(jwt): jwt가 null이 아니고 빈 문자열이 아닌지 확인
            // tokenProvider.validateToken(jwt): jwt가 유효한지 검증
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // 토큰에 포함된 클레임(사용자 아이디, 권한 등)을 파싱하여 Authentication 객체를 생성
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                // 생성한 Authentication 객체를 SecurityContext에 저장 -> 이 후 요청 처리 시, 인증된 상태임을 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("인증 정보 설정 완료: {}", authentication.getName());
            }
        } catch (Exception e) {
            log.error("인증 정보 설정 중 오류 발생: {}", e.getMessage());
            throw new ApplicationException.AuthenticationFailedException(e.getMessage());
        }

        // 현재 필터의 처리가 끝난 후, 요청과 응답을 필터 체인의 다음 필터로 전달
        filterChain.doFilter(request, response);
    }

    /**
     * 클라이언트 요청 헤더에서 JWT 토큰 추출
     * 
     * @param request 클라이언트의 요청 객체
     * @return JWT 토큰
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // HTTP 요청의 "Authorization" 헤더에 저장된 값 추출
        String bearerToken = request.getHeader("Authorization");
        // StringUtils.hasText(jwt): jwt가 null이 아니고 빈 문자열이 아닌지 확인
        // bearerToken.startsWith("Bearer "): 문자열이 "Bearer "라는 접두어로 시작하는지 확인
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 접두어 제거 후 토큰 추출 및 반환
            return bearerToken.substring(7);
        }
        // "Authorization" 헤더에 유효한 JWT 토큰 정보가 없거나, 형식이 올바르지 않은 경우
        return null;
    }
}
