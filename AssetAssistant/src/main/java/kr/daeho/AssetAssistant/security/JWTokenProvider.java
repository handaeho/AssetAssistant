package kr.daeho.AssetAssistant.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import kr.daeho.AssetAssistant.common.exception.ApplicationException;
import kr.daeho.AssetAssistant.auth.service.TokenBlacklistService;

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
    @Value("${jwt.secret-key}")
    private String secretKey;

    /**
     * 엑세스 토큰 만료 시간
     * 
     * @Value: application.properties에서 주입받은 값을 필드에 할당
     */
    @Value("${jwt.token.expiration-time}")
    private long accessTokenValidity;

    /**
     * 리프레시 토큰 만료 시간
     * 
     * @Value: application.properties에서 주입받은 값을 필드에 할당
     */
    @Value("${jwt.refresh-token.expiration-time}")
    private long refreshTokenValidity;

    /**
     * Spring Security의 UserDetailsService 인터페이스를 구현한 SecurityUserDetailService 주입
     * 
     * 사용자 상세 정보를 불러오는 서비스. final로 선언해 불변성 보장
     */
    private final SecurityUserDetailService userDetailsService;

    /**
     * 디바이스 ID 클레임 키 (여러 기기에서 로그인 시 사용)
     */
    private static final String DEVICE_ID_CLAIM = "deviceId";

    /**
     * 토큰 블랙리스트 서비스
     */
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * JWTokenProvider 객체가 생성될 때, 필요한 의존성(토큰 서명용 비밀키, 사용자 정보 서비스)을 전달받아 내부 필드를 초기화
     * 
     * @Autowired: 스프링 컨테이너에서 필요한 의존성 자동 주입
     * @Value: application.properties에서 주입받은 값을 필드에 할당
     * 
     * @param secretKey             JWT 시크릿 키
     * @param userDetailsService    사용자 상세 정보 커스텀 서비스 (인증 세부정보 로드)
     * @param tokenBlacklistService 토큰 블랙리스트 관리 서비스
     */
    @Autowired
    public JWTokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            SecurityUserDetailService userDetailsService,
            TokenBlacklistService tokenBlacklistService) {
        // JWT 생성 및 검증 시 필요한 비밀키 할당
        this.secretKey = secretKey;

        // JWT 토큰 검증 시 필요한 사용자 상세 정보 로드 서비스 할당
        this.userDetailsService = userDetailsService;

        // 토큰 블랙리스트 서비스 할당
        this.tokenBlacklistService = tokenBlacklistService;
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
     * 사용자 ID와 디바이스 ID로 액세스 토큰 생성
     * 
     * @param userId   사용자 ID
     * @param deviceId 디바이스 ID
     * @return JWT 액세스 토큰
     */
    public String generateAccessToken(String userId, String deviceId) {
        // 현재 시간
        Date now = new Date();

        // 액세스 토큰 만료 시간
        Date accessTokenExpirationTime = new Date(now.getTime() + accessTokenValidity);

        // 클레임에 디바이스 ID 클레임 키(deviceId)와 디바이스 ID 추가
        Map<String, Object> claims = new HashMap<>();
        claims.put(DEVICE_ID_CLAIM, deviceId);

        // JWT 토큰 생성
        return Jwts.builder()
                .subject(userId) // 발행 요청 사용자 아이디
                .claims(claims) // 디바이스 ID 포함한 추가 클레임
                .issuedAt(now) // 토큰 발행 시간
                .expiration(accessTokenExpirationTime) // 토큰 만료 시간
                .signWith(getSigningKey()) // 서명 알고리즘 적용
                .compact(); // 최종 토큰 생성
    }

    /**
     * 사용자 ID만으로 액세스 토큰 생성
     * 
     * @param userId 사용자 ID
     * @return JWT 액세스 토큰
     */
    public String generateAccessToken(String userId) {
        // 디바이스 ID 없는 경우 기본값 사용
        return generateAccessToken(userId, "default");
    }

    /**
     * 사용자 ID와 디바이스 ID로 리프레시 토큰 생성
     * 
     * @param userId   사용자 ID
     * @param deviceId 디바이스 ID
     * @return JWT 리프레시 토큰
     */
    public String generateRefreshToken(String userId, String deviceId) {
        // 현재 시간
        Date now = new Date();

        // 리프레시 토큰 만료 시간
        Date refreshTokenExpirationTime = new Date(now.getTime() + refreshTokenValidity);

        // 클레임에 디바이스 ID 클레임 키(deviceId)와 디바이스 ID 추가
        Map<String, Object> claims = new HashMap<>();
        claims.put(DEVICE_ID_CLAIM, deviceId);

        return Jwts.builder()
                .subject(userId) // 발행 요청 사용자 아이디
                .claims(claims) // 디바이스 ID 포함한 추가 클레임
                .issuedAt(now) // 토큰 발행 시간
                .expiration(refreshTokenExpirationTime) // 토큰 만료 시간
                .signWith(getSigningKey()) // 서명 알고리즘 및 시크릿 키
                .compact(); // 최종 토큰 생성
    }

    /**
     * 사용자 ID만으로 리프레시 토큰 생성
     * 
     * @param userId 사용자 ID
     * @return JWT 리프레시 토큰
     */
    public String generateRefreshToken(String userId) {
        // 디바이스 ID 없는 경우 기본값 사용
        return generateRefreshToken(userId, "default");
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
     * JWT 토큰에서 디바이스 ID 추출
     * 
     * @param token JWT 토큰
     * @return 토큰에서 추출한 디바이스 ID, 없으면 "default" 반환
     */
    public String getDeviceIdFromToken(String token) {
        try {
            // JWT 파서로 클레임 추출 및 서명 검증
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey()) // 서명 검증용 시크릿 키 설정
                    .build() // 토큰 파서 빌드
                    .parseSignedClaims(token) // 서명된 클레임 추출(서명 검증 수행)
                    .getPayload(); // 실제 클레임 정보 추출

            // 클레임에서 디바이스 ID 추출, 없으면 기본값 반환
            return claims.get(DEVICE_ID_CLAIM, String.class) != null
                    ? claims.get(DEVICE_ID_CLAIM, String.class)
                    : "default";
        } catch (Exception e) {
            log.error("JWT 토큰에서 디바이스 ID 추출 오류: {}", e.getMessage());
            return "default";
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
     * JWT 토큰 유효성 검증 (블랙리스트 확인 포함)
     * 
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean validateToken(String token) {
        try {
            // 블랙리스트 확인
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.error("블랙리스트에 등록된 토큰입니다.");
                throw new ApplicationException.InvalidTokenException("블랙리스트에 등록된 토큰");
            }

            // JWT 파서로 클레임 추출 및 서명 검증
            Jwts.parser()
                    .verifyWith(getSigningKey()) // 서명 검증용 시크릿 키 설정
                    .build() // 토큰 파서 빌드
                    .parseSignedClaims(token); // 서명된 클레임 추출(서명 검증 수행)

            return true;
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

    /**
     * 리프레시 토큰 만료 시간 가져오기
     * 
     * @return 토큰 만료 시간(초)
     */
    public long getRefreshTokenExpirationTime() {
        return refreshTokenValidity / 1000;
    }

    /**
     * 토큰을 블랙리스트에 추가
     * 
     * @param token 블랙리스트에 추가할 토큰
     */
    public void blacklistToken(String token) {
        try {
            // 토큰의 남은 만료 시간 계산
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            Date now = new Date();

            // 만료 시간이 현재보다 이후인 경우에만 블랙리스트에 추가
            if (expiration != null && expiration.after(now)) {
                // 블랙리스트에 추가 (남은 시간(초) 동안만 유지)
                long ttl = (expiration.getTime() - now.getTime()) / 1000;
                tokenBlacklistService.addToBlacklist(token, ttl);
                log.debug("토큰이 블랙리스트에 추가됨: {}, 만료 시간: {}초", token, ttl);
            }
        } catch (ExpiredJwtException e) {
            // 이미 만료된 토큰은 블랙리스트에 추가할 필요 없음
            log.debug("이미 만료된 토큰은 블랙리스트에 추가하지 않음: {}", token);
        } catch (Exception e) {
            log.error("토큰 블랙리스트 추가 중 오류 발생: {}", e.getMessage());
        }
    }
}
