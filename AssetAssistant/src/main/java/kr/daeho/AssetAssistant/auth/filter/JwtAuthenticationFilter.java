package kr.daeho.AssetAssistant.auth.filter;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import kr.daeho.AssetAssistant.security.JWTokenProvider;
import kr.daeho.AssetAssistant.security.SecurityUserDetailService;

/**
 * JWT 토큰 기반 인증을 처리하는 필터로 OncePerRequestFilter를 상속 받음
 * 
 * OncePerRequestFilter: HTTP 요청 당 한 번만 실행되도록 보장하는 역할
 * 
 * 주로 Spring Security에서 JWT 인증, CORS 처리, 로깅 등 특정 필터 로직을 한 번만 적용하고 싶을 때 사용
 * 
 * [인증 과정 15단계]: 후속 요청에 대한 JWT 인증 처리
 * -> 각 요청이 들어올 때, JWTAuthorizationFilter 같은 커스텀 인증 필터가 JWT를 추출하여 검증
 * -> 토큰이 유효하면, 해당 사용자 정보를 기반으로 SecurityContextHolder에 인증 정보를 설정하여 요청을 처리
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
     * JWT 토큰 관련 유틸리티 클래스
     */
    private final JWTokenProvider jwtTokenProvider;

    /**
     * 사용자 상세 정보 로드 서비스
     */
    private final SecurityUserDetailService userDetailsService;

    /**
     * 모든 HTTP 요청에 대해 JWT 토큰 기반 인증 처리
     * 
     * OncePerRequestFilter의 doFilter 메소드를 오버라이딩
     * 
     * [인증 과정 15단계]: 후속 요청에 대한 JWT 인증 처리
     * 
     * @param request     HTTP 요청
     * @param response    HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException      입출력 예외
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 요청 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);

            // 토큰이 유효한 경우 인증 처리
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                // 토큰에서 사용자 아이디 추출
                String userId = jwtTokenProvider.getUserIdFromToken(jwt);

                // UserDetails 객체 생성 (사용자 정보 로드)
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

                // Authentication 객체 생성
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, // 사용자 정보
                        null, // 자격 증명 (이미 인증되어 null 처리)
                        userDetails.getAuthorities() // 권한 정보
                );

                // 현재 요청 정보 설정
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // [인증 과정 11단계]: SecurityContextHolder에 Authentication 저장
                // SecurityContext에 Authentication 객체 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("JWT 토큰 인증 처리 중 오류 발생: {}", ex.getMessage());
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰 추출
     * 
     * Authorization 헤더에서 Bearer 스킴으로 시작하는 JWT 토큰 추출 (표준 JWT 전송 방식)
     * 
     * @param request HTTP 요청
     * @return JWT 토큰 문자열 또는 null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 접두사 제거 후 토큰 반환
            return bearerToken.substring(7);
        }
        return null;
    }
}
