package kr.daeho.AssetAssistant.security;

import java.util.Collections;
// Static Import: 클래스의 static 멤버(필드나 메서드)를 클래스 이름 없이 바로 사용
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;

import kr.daeho.AssetAssistant.auth.service.TokenBlacklistService;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;

/**
 * JWTokenProvider 단위 테스트 클래스 -> JWTokenProvider 클래스 내 메소드 테스트
 * 
 * Mockito를 사용하여 의존성을 모의(Mock)하고, JUnit을 통해 테스트 케이스를 실행
 * 
 * [Mockito]
 * 1. when(...): 특정 메서드 호출에 대해 예상되는 동작(반환값 또는 예외)을 설정
 * -> public static <T> OngoingStubbing<T> when(T methodCall) { ... }
 * 
 * 2. thenReturn(...): 실제 로직 대신 모의 객체가 반환할 값을 지정
 * -> public static <T> OngoingStubbing<T> thenReturn(T value) { ... }
 * 
 * 3. any(...): 파라미터로 전달되는 객체가 지정된 클래스의 인스턴스라면 어떤 값이든 상관없이 매칭
 * -> public static <T> T any(Class<T> type) { ... }
 * 
 * 4. assertNotNull(...): 특정 객체가 null이 아님을 검증
 * -> public static void assertNotNull(Object object) { ... }
 * 
 * 5. assertThrows(...): 특정 코드 실행 시 지정된 예외가 발생하는지를 검증
 * -> public static <T extends Throwable> T assertThrows(Class<T>
 * exceptionClass, Executable executable) { ... }
 * 
 * 6. assertEquals(...): 예상(expected) 값과 실제(actual) 값이 동일한지를 비교
 * -> public static void assertEquals(Object expected, Object actual) { ... }
 * 
 * 7. verify(...): 모의(mock) 객체의 특정 메서드 호출이 예상대로 이루어졌는지 검증
 * -> public static void verify(Mock<T> mock, VerificationMode mode, Matcher<T>
 * matcher) { ... }
 * 
 * 8. never(...): 모의(mock) 객체의 특정 메서드가 절대 호출되지 않아야 하는지 검증
 * -> public static <T> T never() { ... }
 * 
 * 9. any(): 특정 메서드 호출 시 인자의 실제 값은 중요하지 않을 때, 그 자리에서 "어떤 값이든 상관없다"라고 지정
 * -> 단, 타입은 정확하게 전달 되어야 함
 * -> public static <T> T any(Class<T> type) { ... }
 * 
 * ex)
 * when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);
 * -> userRepository.save() 메서드가 호출될 때, UserEntity 타입을 가진 어떤 객체가 전달되더라도
 * testUserEntity를 반환하도록 설정
 * -> 인자의 타입만 지정하면, 실제 전달되는 값은 무시.
 * -> 값의 구체적인 내용보다 메서드 호출 자체가 이루어졌는지, 그리고 호출 횟수 등을 검증할 때 유용
 * 
 * [Given - when - Then: Behavior-Driven Development(BDD)의 테스트 시나리오 기술 패턴]
 * 1. Given: 테스트가 시작되기 전에 필요한 초기 상태나 조건을 설정하는 단계
 * -> DB에 특정 사용자 지정, 테스트 할 시스템의 환경 설정 등
 * 
 * 2. When: 실제 테스트 대상 행동을 수행하는 단계. 시스템 상호작용, 특정 메소드 호출 등
 * -> 사용자가 로그인 폼에 정보를 입력 후 로그인 버튼 클릭, 특정 API 엔드포인트에 요청 전송 등
 * 
 * 3. Then: 행동(When)이 실행된 후 기대되는 결과나 상태를 검증하는 단계
 * -> 로그인 성공 시 JWT 토큰 반환, API 요청에 대한 올바른 응답 반환 등
 * 
 * @ExtendWith: JUnit 5 테스트 실행 시, 테스트 클래스나 메서드에 특정 확장의 기능을 추가
 *              - MockitoExtension.class: Mockito 확장을 사용하도록 설정
 */
@ExtendWith(MockitoExtension.class)
public class JWTokenProviderTest {
    /**
     * 테스트 대상 객체
     * 
     * @InjectMocks: 모의 객체들을 주입받는 실제 객체
     */
    @InjectMocks
    private JWTokenProvider tokenProvider;

    /**
     * 모의 객체 - SecurityUserDetailService
     * 
     * Spring Security의 UserDetailsService 인터페이스 구현 서비스 클래스
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private SecurityUserDetailService userDetailsService;

    /**
     * 모의 객체 - UserDetails
     * 
     * Spring Security의 UserDetails 인터페이스 구현 클래스
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private UserDetails userDetails;

    /**
     * 모의 객체 - TokenBlacklistService
     * 
     * 토큰 블랙리스트 관리 서비스 클래스
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    /**
     * 테스트 데이터
     */
    private final String TEST_USER_ID = "testUser";
    private final String TEST_DEVICE_ID = "testDevice";
    private final String TEST_SECRET_KEY = "thisIsAVeryLongSecretKeyForTestingPurposesOnlyDoNotUseInProduction";

    /**
     * 각 테스트 전에 실행되는 설정 메소드
     * 
     * @BeforeEach: 각 테스트 메소드 실행 전에 호출됨
     */
    @BeforeEach
    void setUp() {
        // 테스트용 시크릿 키와 토큰 만료 시간 설정
        // ReflectionTestUtils: 테스트 코드에서 프라이빗 필드에 접근하고 설정할 수 있도록 돕는 유틸리티 클래스
        ReflectionTestUtils.setField(tokenProvider, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(tokenProvider, "accessTokenValidity", 3600000L); // 1시간
        ReflectionTestUtils.setField(tokenProvider, "refreshTokenValidity", 604800000L); // 7일
    }

    /**
     * 액세스 토큰 생성 테스트
     * 
     * 시나리오: 사용자 아이디와 디바이스 아이디로 액세스 토큰 생성
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("액세스 토큰 생성 테스트")
    void generateAccessTokenTest() {
        // When - 액세스 토큰 생성
        String accessToken = tokenProvider.generateAccessToken(TEST_USER_ID, TEST_DEVICE_ID);

        // Then - 액세스 토큰 생성 검증
        assertNotNull(accessToken, "액세스 토큰이 생성되어야 함 (NULL이 아님).");
        assertTrue(accessToken.length() > 0, "액세스 토큰이 비어있지 않아야 함.");

        // 토큰에서 사용자 ID와 디바이스 ID 추출 확인
        assertEquals(TEST_USER_ID, tokenProvider.getUserIdFromToken(accessToken),
                "액세스 토큰에서 사용자 ID를 올바르게 추출해야 함.");
        assertEquals(TEST_DEVICE_ID, tokenProvider.getDeviceIdFromToken(accessToken),
                "액세스 토큰에서 디바이스 ID를 올바르게 추출해야 함.");
    }

    /**
     * 리프레시 토큰 생성 테스트
     * 
     * 시나리오: 사용자 아이디와 디바이스 아이디로 리프레시 토큰 생성
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("리프레시 토큰 생성 테스트")
    void generateRefreshTokenTest() {
        // When - 리프레시 토큰 생성
        String refreshToken = tokenProvider.generateRefreshToken(TEST_USER_ID, TEST_DEVICE_ID);

        // Then - 리프레시 토큰 생성 검증
        assertNotNull(refreshToken, "리프레시 토큰이 생성되어야 함 (NULL이 아님).");
        assertTrue(refreshToken.length() > 0, "리프레시 토큰이 비어있지 않아야 함.");

        // 토큰에서 사용자 ID와 디바이스 ID 추출 확인
        assertEquals(TEST_USER_ID, tokenProvider.getUserIdFromToken(refreshToken),
                "리프레시 토큰에서 사용자 ID를 올바르게 추출해야 함.");
        assertEquals(TEST_DEVICE_ID, tokenProvider.getDeviceIdFromToken(refreshToken),
                "리프레시 토큰에서 디바이스 ID를 올바르게 추출해야 함.");
    }

    /**
     * 유효한 토큰 검증 테스트
     * 
     * 시나리오: 토큰 검증 시, 토큰이 유효하고 블랙리스트에 토큰이 없음
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("유효한 토큰 검증 테스트")
    void validateTokenSuccessTest() {
        // Given - 토큰 유효성 검증 테스트 준비
        // 토큰 생성
        String token = tokenProvider.generateAccessToken(TEST_USER_ID, TEST_DEVICE_ID);

        // 토큰 블랙리스트 서비스에서 토큰이 블랙리스트에 없음을 확인
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);

        // When - 토큰 유효성 확인
        boolean isValid = tokenProvider.validateToken(token);

        // Then - 토큰 유효성 검증
        assertTrue(isValid, "유효한 토큰이 검증되어야 함.");

        // tokenBlacklistService의 블랙리스트 확인 메소드 호출 검증
        verify(tokenBlacklistService).isBlacklisted(token);
    }

    /**
     * 토큰 블랙리스트 추가 테스트
     * 
     * 시나리오: 토큰을 블랙리스트 추가
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("토큰 블랙리스트 추가 테스트")
    void blacklistTokenTest() {
        // Given - 토큰 블랙리스트 추가 테스트 준비
        // 토큰 생성
        String token = tokenProvider.generateAccessToken(TEST_USER_ID, TEST_DEVICE_ID);

        // 블랙리스트에 토큰 추가 (doNothing(): void 메소드를 모킹 (addToBlacklist))
        doNothing().when(tokenBlacklistService).addToBlacklist(eq(token), anyLong());

        // When - 토큰을 블랙리스트 추가
        tokenProvider.blacklistToken(token);

        // Then - 토큰 블랙리스트 추가 메소드 호출 검증
        verify(tokenBlacklistService).addToBlacklist(eq(token), anyLong());
    }

    /**
     * 블랙리스트에 있는 토큰 검증 테스트
     * 
     * 시나리오: 블랙리스트에 있는 토큰 검증 시, 예외가 발생해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("블랙리스트에 있는 토큰 검증 테스트")
    void validateTokenBlacklistedFailureTest() {
        // Given - 블랙리스트에 있는 토큰 검증 테스트 준비
        // 토큰 생성
        String token = tokenProvider.generateAccessToken(TEST_USER_ID, TEST_DEVICE_ID);

        // 토큰 블랙리스트 서비스에서 토큰이 블랙리스트에 있음을 확인
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

        // When & Then - 토큰 블랙리스트 검증 실패 시, 예외 발생 검증
        assertThrows(ApplicationException.AuthenticationFailedException.class,
                () -> tokenProvider.validateToken(token),
                "블랙리스트에 있는 토큰으로 tokenProvider의 validateToken 메소드 호출 시 AuthenticationFailedException 예외 발생");

        // tokenBlacklistService의 블랙리스트 확인 메소드 호출 검증
        verify(tokenBlacklistService).isBlacklisted(token);
    }

    /**
     * 잘못된 형식의 토큰 검증 테스트
     * 
     * 시나리오: 잘못된 형식의 토큰 검증 시, 예외가 발생해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("잘못된 형식의 토큰 검증 테스트")
    void validateTokenMalformedFailureTest() {
        // Given - 잘못된 형식의 토큰 검증 실패 테스트 준비

        // 잘못된 형식의 토큰 생성
        String malformedToken = "malformed.token.string";

        // When & Then - 잘못된 형식의 토큰 검증 실패 시, 예외 발생 검증
        // 잘못된 토큰으로 tokenProvider의 validateToken 메소드 호출 시 예외 발생
        assertThrows(Exception.class, () -> tokenProvider.validateToken(malformedToken),
                "잘못된 형식의 토큰으로 tokenProvider의 validateToken 메소드 호출 시 예외 발생");
    }

    /**
     * 토큰에서 사용자 ID 추출 테스트
     * 
     * 시나리오: 토큰에서 사용자 ID 추출 시, 사용자 ID가 올바르게 추출되어야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("토큰에서 사용자 ID 추출 테스트")
    void getUserIdFromTokenTest() {
        // Given - 토큰에서 사용자 ID 추출 테스트 준비
        // 토큰 생성
        String token = tokenProvider.generateAccessToken(TEST_USER_ID, TEST_DEVICE_ID);

        // When - 토큰에서 사용자 ID 추출
        String userId = tokenProvider.getUserIdFromToken(token);

        // Then - 토큰에서 사용자 ID 추출 검증
        assertEquals(TEST_USER_ID, userId, "토큰에서 사용자 ID를 올바르게 추출해야 함.");
    }

    /**
     * 토큰에서 디바이스 ID 추출 테스트
     * 
     * 시나리오: 토큰에서 디바이스 ID 추출 시, 디바이스 ID가 올바르게 추출되어야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("토큰에서 디바이스 ID 추출 테스트")
    void getDeviceIdFromTokenTest() {
        // Given - 토큰에서 디바이스 ID 추출 테스트 준비
        // 토큰 생성
        String token = tokenProvider.generateAccessToken(TEST_USER_ID, TEST_DEVICE_ID);

        // When - 토큰에서 디바이스 ID 추출
        String deviceId = tokenProvider.getDeviceIdFromToken(token);

        // Then - 토큰에서 디바이스 ID 추출 검증
        assertEquals(TEST_DEVICE_ID, deviceId, "토큰에서 디바이스 ID를 올바르게 추출해야 함.");
    }

    /**
     * 토큰으로 인증 객체 생성 테스트
     * 
     * 시나리오: 토큰으로 인증 객체 생성 시, 인증 객체(Authentication)가 NULL이 아님
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("토큰으로 인증 객체 생성 테스트")
    void getAuthenticationTest() {
        // Given - 토큰으로 인증 객체 생성 테스트 준비
        // 토큰 생성
        String token = tokenProvider.generateAccessToken(TEST_USER_ID, TEST_DEVICE_ID);

        // 사용자 아이디로 userDetailsService의 loadUserByUsername 메소드 호출 시, userDetails 객체 반환
        when(userDetailsService.loadUserByUsername(TEST_USER_ID)).thenReturn(userDetails);

        // userDetails의 getAuthorities 메소드 호출 시, 권한 정보 빈 리스트 반환
        // 토큰에서 사용자 정보 추출 후, 인증 객체 생성 여부 테스트가 목적이므로 빈 리스트로 반환
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        // When - 토큰으로 인증 객체 생성
        Authentication auth = tokenProvider.getAuthentication(token);

        // Then - 토큰으로 인증 객체 생성 검증
        assertNotNull(auth, "토큰으로 인증 객체 생성 시 인증 객체가 NULL이 아님.");

        // 사용자 아이디로 userDetailsService의 loadUserByUsername 메소드 호출 검증
        verify(userDetailsService).loadUserByUsername(TEST_USER_ID);
    }
}
