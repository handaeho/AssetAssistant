package kr.daeho.AssetAssistant.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성, 검증, 파싱을 담당하는 유틸리티 클래스
 * 
 * 주요 기능:
 * - 토큰 생성: 사용자 인증 정보를 기반으로 JWT 발급
 * - 토큰 검증: 전달받은 토큰의 유효성 확인(위조 검사, 만료 확인)
 * - 정보 추출: 토큰에서 사용자 이름(주체) 및 클레임 정보 추출
 * 
 * JWT 구조:
 * Header(알고리즘, 토큰 타입) + Payload(클레임) + Signature(서명)
 * 
 * 클레임(Claim) 종류:
 * - 등록 클레임: iss(발급자), exp(만료시간), sub(주제), aud(대상자) 등
 * - 공개 클레임: 충돌 방지를 위한 URI 형식
 * - 비공개 클레임: 사용자 정의 클레임(ex: userId, role)
 * 
 * [인증 과정]의 13, 15번 단계
 * 
 * 13. AuthenticationSuccessHandler에서 JWT 발급 수행
 * -> 인증 성공 후, 커스텀 AuthenticationSuccessHandler (또는 필터 내 로직)에서
 * -> 사용자 정보(예: 아이디, 권한 등)를 기반으로 JWT를 생성
 * -> 생성된 JWT 토큰을 HTTP 응답 헤더(예: Authorization)나 응답 본문에 포함하여 클라이언트에게 전달
 * 
 * 15. 후속 요청에 대한 JWT 인증 처리
 * -> 각 요청이 들어올 때, JWTAuthorizationFilter 같은 커스텀 인증 필터가 JWT를 추출하여 검증
 * -> 토큰이 유효하면, 해당 사용자 정보를 기반으로 SecurityContextHolder에 인증 정보를 설정하여 요청을 처리
 * 
 * @Component: 스프링 컨테이너에 빈으로 등록
 *             - Bean: 스프링 컨테이너에 의해 관리되는 객체
 *             - 범용적인 빈 등록을 위한 어노테이션. 특별한 프록시 처리는 하지 않음
 *             - 설정 관련 빈 등록 및 프록시 설정을 위한 @Configuration 컴포넌트보다 범용적으로 사용하는 용도
 * @Slf4j: 로깅을 위한 어노테이션
 */
@Component
@Slf4j
public class JWTokenProvider {
    private final String secretKey;
    private final long tokenValidityInMilliseconds;
    private final UserDetailsService userDetailsService;

    /**
     * 생성자 주입 방식으로 JWTokenProvider 의존성 주입
     * 
     * @Value: 프로퍼티 값 주입 어노테이션
     *         - properties, yml 등 설정 파일에서 정의한 값을 불러옴 (민감한 값, 자주 바뀌는 값 등)
     *         - 하드코딩으로 매번 값을 입력하는 것보다 설정 파일에 정의하고 사용하는 것이 관리하기 좋음
     * 
     * @param secretKey              시크릿 키 값
     * @param tokenValidityInSeconds 토큰 유효 기간
     * @param userDetailsService     사용자 정보 서비스
     */
    public JWTokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds,
            UserDetailsService userDetailsService) {
        this.secretKey = secretKey;
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
        this.userDetailsService = userDetailsService;
    }

    /**
     * 인증 정보를 기반으로 JWT 토큰 생성
     * 
     * [인증 과정 13단계]: 사용자 정보(예: 아이디, 권한 등)를 기반으로 JWT를 생성
     * 
     * [생성 절차]
     * 
     * 1. 사용자명 추출(등록 클레임의 주체, 토큰 식별자)
     * 
     * 2. 현재 시간 기준 만료시간 계산
     * 
     * 3. HS256 알고리즘으로 시크릿 키 생성
     * 
     * 4. JWT 빌더를 이용한 토큰 조립
     * 
     * @param authentication 스프링 시큐리티 인증 객체
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateToken(Authentication authentication) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * JWT 토큰에서 사용자 아이디 추출
     */
    public String getUserIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            log.error("토큰에서 사용자 아이디 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * JWT 토큰 유효성 검증
     * 
     * [검증 항목]
     * 
     * 1. 서명 위조 여부
     * 
     * 2. 토큰 구조 유효성
     * 
     * 3. 만료 시간 확인
     * 
     * @param token 검증할 JWT 토큰(문자열)
     * @return 유효성 여부 (true: 유효, false: 무효)
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException ex) {
            // 토큰 변조 또는 제공된 시크릿키와 실제 토큰 서명이 다를 때
            log.error("유효하지 않은 JWT 서명: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            // 토큰 구조가 올바르지 않을 때 (헤더, 페이로드, 서명) -> 문자열 손상, 인코딩 에러 등
            log.error("JWT 토큰 구조가 올바르지 않음.: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            // JWT 토큰 만료 (유효기간 경과)
            log.error("만료된 JWT 토큰: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            // 현재 어플리케이션에서 지원하지 않는 JWT 토큰 형식 또는 알고리즘
            log.error("지원하지 않는 JWT 토큰: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // 전달된 JWT 토큰 문자열이 NULL 또는 빈 문자열, 클레임 부분이 비어있을 경우
            log.error("JWT 토큰 문자열이 비어있음: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * JWT 토큰에서 사용자 이름(주체) 및 클레임 정보 추출
     * 
     * [인증 과정 15단계]: JWT 토큰을 검증하고 사용자 정보 추출
     * 
     * @param token 토큰 문자열
     * @return 사용자 이름(주체) 및 클레임 정보
     */
    public Authentication getAuthentication(String token) {
        String userId = getUserIdFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}
