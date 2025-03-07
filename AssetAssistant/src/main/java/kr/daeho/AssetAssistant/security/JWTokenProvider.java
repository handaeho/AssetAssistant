package kr.daeho.AssetAssistant.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증 클래스
 * 
 * 이 클래스는 JWT 토큰의 생성, 유효성 검증, 사용자 정보 추출 등 JWT 관련 모든 기능을 제공합니다.
 * 
 * JWT(JSON Web Token)는 사용자 인증 정보를 안전하게 전송하기 위한 개방형 표준(RFC 7519)입니다.
 * 토큰은 헤더(Header), 페이로드(Payload), 서명(Signature)의 세 부분으로 구성됩니다.
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
    private long accessTokenExpirationTime;

    /**
     * 리프레시 토큰 만료 시간
     * 
     * @Value: application.properties에서 주입받은 값을 필드에 할당
     */
    @Value("${jwt.refresh-token-expiration-time}")
    private long refreshTokenExpirationTime;

    /**
     * 사용자 상세 정보 서비스
     */
    private final SecurityUserDetailService userDetailsService;

    /**
     * 생성자 주입
     * 
     * @param userDetailsService 사용자 상세 정보 서비스
     */
    public JWTokenProvider(SecurityUserDetailService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * 서명 키 생성 - 문자열 시크릿 키를 HMAC-SHA 알고리즘으로 변환
     * 
     * @return 서명 키
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
