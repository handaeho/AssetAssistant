package kr.daeho.AssetAssistant.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import kr.daeho.AssetAssistant.common.exception.ApplicationException;

/**
 * JWT 토큰 생성 및 검증 클래스
 * 
 * 이 클래스는 JWT 토큰의 생성, 유효성 검증, 사용자 정보 추출 등 JWT 관련 모든 기능을 제공합니다.
 * 
 * JWT(JSON Web Token)는 사용자 인증 정보를 안전하게 전송하기 위한 개방형 표준(RFC 7519).
 * 토큰은 헤더(Header), 페이로드(Payload), 서명(Signature)의 세 부분으로 구성
 * 
 * @Component: 스프링에서 관리하는 컴포넌트로 등록
 * @Slf4j: 로깅을 위한 Lombok 어노테이션
 */
@Component
@Slf4j
public class JWTokenProvider {
    /**
     * JWT 시크릿 키 (application.properties에서 주입)
     * 이 키는 토큰 서명에 사용되며, 이를 통해 토큰의 위변조를 방지
     * 
     * @Value: application.properties에서 주입받은 값을 필드에 할당
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * 엑세스 토큰 만료 시간
     * 
     * @Value: application.properties에서 주입받은 값을 필드에 할당
     */
    @Value("${jwt.access-token-expiration-time}")
    private long accessTokenValidity;

    /**
     * 리프레시 토큰 만료 시간
     * 
     * @Value: application.properties에서 주입받은 값을 필드에 할당
     */
    @Value("${jwt.refresh-token-expiration-time}")
    private long refreshTokenValidity;

    /**
     * Spring Security의 UserDetailsService 인터페이스를 구현한 SecurityUserDetailService 주입
     * 
     * 사용자 상세 정보를 불러오는 서비스. final로 선언해 불변성 보장
     */
    private final SecurityUserDetailService userDetailsService;

    /**
     * SecurityUserDetailService 생성자 주입
     * 
     * @param userDetailsService 사용자 상세 정보 서비스
     */
    public JWTokenProvider(SecurityUserDetailService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * 서명 키 생성 - 문자열 시크릿 키를 HMAC-SHA 알고리즘으로 변환
     * 
     * @return 알고리즘이 적용된 암호화 서명 키
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT 액세스 토큰 생성
     * 
     * Authentication: 사용자 인증정보를 캡슐화하는 인터페이스
     * - Authentication.getPrincipal(): 인증대상(사용자 정보)
     * - Authentication.getCredentials(): 인증 자격증명(비밀번호)
     * - Authentication.getAuthorities(): 사용자 권한 목록
     * 
     * 로그인 요청 시, UsernamePasswordAuthenticationToken 처럼,
     * Authentication 구현한 객체를 생성해 인증요청
     * 
     * @param authentication 인증 정보
     * @return 생성된 JWT 액세스 토큰
     */
    public String generateAccessToken(Authentication authentication) {
        // 현재 시간
        Date nowTime = new Date();

        // 액세스 토큰 만료 시간
        Date accessTokenExpirationTime = new Date(nowTime.getTime() + accessTokenValidity);

        log.debug("액세스 토큰 생성: 사용자={}, 만료시간={}", authentication.getName(), accessTokenExpirationTime);

        // JWT 토큰 생성
        return Jwts.builder()
                .subject(authentication.getName()) // 발행 요청 사용자 아이디
                .issuedAt(nowTime) // 토큰 발행 시간
                .expiration(accessTokenExpirationTime) // 토큰 만료 시간
                .signWith(getSigningKey()) // 서명 알고리즘 및 시크릿 키
                .compact(); // 최종 토큰 생성
    }

    /**
     * JWT 리프레시 토큰 생성
     * 
     * Authentication: 사용자 인증정보를 캡슐화하는 인터페이스
     * - Authentication.getPrincipal(): 인증대상(사용자 정보)
     * - Authentication.getCredentials(): 인증 자격증명(비밀번호)
     * - Authentication.getAuthorities(): 사용자 권한 목록
     * 
     * 로그인 요청 시, UsernamePasswordAuthenticationToken 처럼,
     * Authentication 구현한 객체를 생성해 인증요청
     * 
     * @param authentication 인증 정보
     * @return 생성된 JWT 액세스 토큰
     */
    public String generateRefreshToken(Authentication authentication) {
        // 현재 시간
        Date nowTime = new Date();

        // 리프레시 토큰 만료 시간
        Date refreshTokenExpirationTime = new Date(nowTime.getTime() + refreshTokenValidity);

        log.debug("리프레시 토큰 생성: 사용자={}, 만료시간={}", authentication.getName(), refreshTokenExpirationTime);

        // JWT 토큰 생성
        return Jwts.builder()
                .subject(authentication.getName()) // 발행 요청 사용자 아이디
                .issuedAt(nowTime) // 토큰 발행 시간
                .expiration(refreshTokenExpirationTime) // 토큰 만료 시간
                .signWith(getSigningKey()) // 서명 알고리즘 및 시크릿 키
                .compact(); // 최종 토큰 생성
    }

    /**
     * JWT 토큰에서 사용자 아이디(발행 주체, subject) 추출
     * 
     * @param token JWT 토큰
     * @return 토큰에서 추출한 사용자 아이디
     */
    public String getUserIdFromToken(String token) {
        try {
            // JWT 파서로 클레임 추출 및 서명 검증
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey()) // 서명 검증용 시크릿 키 설정
                    .build() // 토큰 파서 빌드
                    .parseSignedClaims(token) // 서명된 클레임 추출(서명 검증 수행)
                    .getPayload(); // 실제 클레임 정보 추출 (subject, issuedAt, expiration 등)

            // 클레임에서 사용자 아이디 추출
            return claims.getSubject();
        } catch (Exception e) {
            log.error("JWT 토큰 파싱 오류: {}", e.getMessage());
            throw new ApplicationException.GetUserIdFromTokenFailedException(e.getMessage());
        }
    }

    /**
     * JWT 토큰으로부터 인증 객체 생성
     * 
     * UsernamePasswordAuthenticationToken: 사용자 인증정보를 캡슐화하는 인터페이스
     * -> Spring Security의 Authentication 인터페이스 구현체
     * 
     * - userDetails: 사용자 상세 정보 (아이디, 비밀번호 등) -> Authentication 객체의 principal(주체)
     * - "" (빈 문자열): 자격 증명(credentials) -> 인증 완료 후, 비밀번호 사용 하지 않아 빈 문자열
     * - userDetails.getAuthorities(): 사용자 권한 -> Authentication 객체의 authorities(권한)
     * 
     * @param token JWT 토큰
     * @return 인증 객체
     */
    public Authentication getAuthentication(String token) {
        // 토큰에서 사용자 아이디 추출
        String userId = getUserIdFromToken(token);

        // UserDetailsService에서 사용자 정보 획득
        // UserDetails: 사용자 정보를 캡슐화하는 인터페이스
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        // 인증 객체 생성
        return new UsernamePasswordAuthenticationToken(
                userDetails, "", userDetails.getAuthorities());
    }

    /**
     * JWT 토큰 유효성 검증
     * 
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean validateToken(String token) {
        try {
            // JWT 파서로 클레임 추출 및 서명 검증
            Jwts.parser()
                    .verifyWith(getSigningKey()) // 서명 검증용 시크릿 키 설정
                    .build() // 토큰 파서 빌드
                    .parseSignedClaims(token); // 서명된 클레임 추출(서명 검증 수행)

            return true;
            // TODO: 이 부분 확인 -> The type SignatureException is deprecated
        } catch (SignatureException e) {
            // 토큰 변조 또는 제공된 시크릿키와 실제 토큰 서명이 다를 때
            log.error("유효하지 않은 JWT 서명: {}", e.getMessage());
            throw new ApplicationException.InvalidTokenException(e.getMessage());
        } catch (MalformedJwtException ex) {
            // 토큰 형식이 잘못됐을 때
            log.error("유효하지 않은 JWT 토큰: {}", ex.getMessage());
            throw new ApplicationException.InvalidTokenException(ex.getMessage());
        } catch (ExpiredJwtException ex) {
            // 토큰 만료되었을 때
            log.error("만료된 JWT 토큰: {}", ex.getMessage());
            throw new ApplicationException.TokenExpiredException(ex.getMessage());
        } catch (SecurityException ex) {
            // 사용된 알고리즘이 예상과 다를 때
            log.error("지원되지 않는 JWT 토큰: {}", ex.getMessage());
            throw new ApplicationException.InvalidTokenAlgorithmException(ex.getMessage());
        } catch (Exception ex) {
            // 그 외 모든 예외
            log.error("JWT 토큰 검증 또는 인증 실패: {}", ex.getMessage());
            throw new ApplicationException.AuthenticationFailedException(ex.getMessage());
        }
    }

    /**
     * 액세스 토큰 만료 시간 가져오기
     * 
     * @return 토큰 만료 시간(초)
     */
    public long getTokenExpirationTime() {
        return accessTokenValidity / 1000;
    }
}
