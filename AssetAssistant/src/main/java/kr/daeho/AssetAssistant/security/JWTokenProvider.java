package kr.daeho.AssetAssistant.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

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
    /**
     * JWT 시크릿 키 주입
     * 
     * properties 나 yml 파일에 비밀키의 원문을 작성하고, 이를 암호화 해 비밀키 생성에 사용
     * 
     * jwtSecret 문자열은 최소 256비트(32바이트) 이상 (아니면 예외 발생)
     * 
     * @Value: 스프링 컨테이너가 관리하는 빈(컴포넌트) 내부에서 외부 설정값을 주입받을 때 사용
     *         - 예: application.properties 또는 application.yml, 환경 변수 등
     */
    @Value("${jwt.secret-key}")
    private String jwtSecret;

    /**
     * 토큰 유효기간(밀리초 단위, 기본 1시간) 주입
     * 
     * @Value: 스프링 컨테이너가 관리하는 빈(컴포넌트) 내부에서 외부 설정값을 주입받을 때 사용
     *         - 예: application.properties 또는 application.yml, 환경 변수 등
     */
    @Value("${jwt.expire-length}")
    private long jwtExpirationMs;

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
        // 사용자 이름 추출 (등록 클레임의 주체, 토큰 식별자)
        String username = authentication.getName();

        // 현재 시간 및 토큰 만료 시간 계산
        Date nowDate = new Date();
        Date expiryDate = new Date(nowDate.getTime() + jwtExpirationMs);

        // 주입된 jwtSecret 문자열을 바탕으로 HS256 알고리즘에 적합한 시크릿 키 생성
        // jwtSecret 문자열 -> 바이트 배열로 변환 -> HS256 알고리즘에 맞는 시크릿 키 생성
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // JWT 빌더를 사용한 토큰 생성 및 반환
        // Jwts: JWT 토큰 생성 및 검증을 위한 유틸리티 클래스 (jjwt에서 지원)
        return Jwts.builder()
                .subject(username) // 주체 설정
                .issuedAt(nowDate) // 토큰 발행 시간
                .expiration(expiryDate) // 토큰 만료 시간
                .signWith(key) // 시크릿 키
                .compact(); // 최종 토큰 생성
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
            // 주입된 jwtSecret 문자열을 바탕으로 HS256 알고리즘에 적합한 시크릿 키 생성
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            // JWT 파서 생성(JWT 구조 파싱 및 클레임 추출)
            Jwts.parser()
                    // 서명 검증을 위한 SecretKey 설정
                    .verifyWith(key)
                    // 파서 인스턴스 생성
                    .build()
                    // 서명 검증 포함 파싱 (토큰 구조 해석 및 SecretKey로 위변조 확인, 클레임 추출)
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
    public String getUserIdFromToken(String token) {
        // 주입된 jwtSecret 문자열을 바탕으로 HS256 알고리즘에 적합한 시크릿 키 생성
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                // 서명 검증을 위한 SecretKey 설정
                .verifyWith(key)
                // 파서 인스턴스 생성
                .build()
                // 서명 검증 포함 파싱 (토큰 구조 해석 및 SecretKey로 위변조 확인, 클레임 추출)
                .parseSignedClaims(token)
                // 클레임(토큰에 들어있는 각종 정보(등록 클레임, 공개 클레임, 비공개 클레임)) 추출
                .getPayload()
                // 주체(사용자 아이디) 추출
                .getSubject();
    }
}
