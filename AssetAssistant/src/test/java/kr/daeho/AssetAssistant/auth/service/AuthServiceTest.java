package kr.daeho.AssetAssistant.auth.service;

// Static Import: 클래스의 static 멤버(필드나 메서드)를 클래스 이름 없이 바로 사용
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import kr.daeho.AssetAssistant.auth.dto.LoginRequestDto;
import kr.daeho.AssetAssistant.auth.dto.TokenResponseDto;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;
import kr.daeho.AssetAssistant.security.JWTokenProvider;

/**
 * AuthService 단위 테스트 클래스 -> AuthService 클래스 내 메소드 테스트
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
public class AuthServiceTest {
    /**
     * 테스트 대상 객체
     * 
     * @InjectMocks: 모의 객체들을 주입받는 실제 객체
     */
    @InjectMocks
    private AuthService authService;

    /**
     * 모의 객체 - 인증 관리자
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private AuthenticationManager authenticationManager;

    /**
     * 모의 객체 - JWT 토큰 제공자
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private JWTokenProvider tokenProvider;

    /**
     * 모의 객체 - 인증 객체
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private Authentication authentication;

    /**
     * 테스트 데이터
     */
    private LoginRequestDto loginRequestDto;
    private final String TEST_USER_ID = "testUserId";
    private final String TEST_PASSWORD = "testPassword";
    private final String TEST_ACCESS_TOKEN = "testAccessToken";
    private final String TEST_REFRESH_TOKEN = "testRefreshToken";
    private final long TEST_EXPIRES_IN = 3600;

    /**
     * 각 테스트 전에 실행되는 설정 메소드
     * 
     * @BeforeEach: 각 테스트 메소드 실행 전에 호출됨
     */
    @BeforeEach
    void setUp() {
        // 테스트용 로그인 요청 DTO 생성 (테스트 아이디, 패스워드 입력)
        loginRequestDto = LoginRequestDto.builder()
                .userId(TEST_USER_ID)
                .password(TEST_PASSWORD)
                .build();
    }

    /**
     * 로그인 성공 테스트
     * 
     * 시나리오: 올바른 사용자 아이디/비밀번호로 로그인하면 올바른 액세스 토큰과 리프레시 토큰을 반환해야 함
     * 
     * authenticationManager.authenticate(...): 실제 인증 로직을 수행하는 메서드
     * 
     * UsernamePasswordAuthenticationToken:
     * -> Spring Security의 Authentication 인터페이스 구현체 (인증 요청 및 결과 정보 캡슐화)
     * -> 인증 전: 인증되지 않은 상태로 생성 (principal(사용자 이름)과 credentials(비밀번호)만 설정)
     * -> 인증 후: 사용자 권한(authorities) 정보를 포함 및 authenticated 플래그가 true로 설정
     * -> getPrincipal(): 사용자 이름 등 사용자 정보 반환
     * -> getCredentials(): 사용자 비밀번호 등 자격증명 정보 반환
     * -> getDetails(): 사용자 권한 등 추가 정보 등 반환
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("로그인 성공 시 액세스 토큰 및 리프레시 토큰 반환")
    void loginSuccess() {
        // Given - 로그인 테스트 준비
        // 인증 관리자인 authenticationManager가 인증 객체를 반환하도록 설정
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // 토큰 제공자인 tokenProvider가 액세스 토큰과 리프레시 토큰을 반환하도록 설정
        when(tokenProvider.generateAccessToken(authentication)).thenReturn(TEST_ACCESS_TOKEN); // 액세스 토큰
        when(tokenProvider.generateRefreshToken(authentication)).thenReturn(TEST_REFRESH_TOKEN); // 리프레시 토큰
        when(tokenProvider.getTokenExpirationTime()).thenReturn(TEST_EXPIRES_IN); // 토큰 만료시간

        // When - 로그인 테스트 실행
        // 로그인 서비스를 호출하고, 반환된 토큰 응답 TokenResponseDto 객체 저장
        TokenResponseDto tokenResponseDto = authService.login(loginRequestDto);

        // Then - 로그인 테스트 결과 검증
        assertNotNull(tokenResponseDto, "로그인 성공시, tokenResponseDto가 NULL이 아님.");
        assertEquals(TEST_ACCESS_TOKEN, tokenResponseDto.getAccessToken(), "로그인 성공시, 액세스 토큰이 일치함.");
        assertEquals(TEST_REFRESH_TOKEN, tokenResponseDto.getRefreshToken(), "로그인 성공시, 리프레시 토큰이 일치함.");
        assertEquals(TEST_EXPIRES_IN, tokenResponseDto.getExpiresIn(), "로그인 성공시, 토큰 만료시간이 일치함.");
        assertEquals("Bearer", tokenResponseDto.getTokenType(), "로그인 성공시, 토큰 타입이 Bearer 타입임.");

        // 인증 관리자인 authenticationManager의 authenticate 메서드가 호출되었는지 검증
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // 액세스 및 리프레시 토큰 생성 메소드가 호출되었는지 검증
        verify(tokenProvider).generateAccessToken(authentication);
        verify(tokenProvider).generateRefreshToken(authentication);
    }

    /**
     * 로그인 실패 테스트 - 잘못된 자격 증명
     * 
     * 시나리오: 잘못된 비밀번호로 로그인하면 예외가 발생해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("잘못된 자격 증명으로 로그인 실패")
    void loginFailure_BadCredentials() {
        // Given - 자격증명 오류로 인한 로그인 실패 테스트 준비
        // 인증 관리자인 authenticationManager가 BadCredentialsException 예외를 반환하도록 설정
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("잘못된 자격증명."));

        // When & Then - 자격증명 오류로 인한 로그인 실패 테스트 실행 및 예외 검증
        assertThrows(ApplicationException.LoginFailedException.class,
                () -> authService.login(loginRequestDto),
                "로그인 실패시, LoginFailedException 예외가 발생해야 함.");

        // 인증 관리자인 authenticationManager의 authenticate 메서드가 호출되었는지 검증
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // 토큰 생성 메소드들이 호출되지 않았는지 검증
        verify(tokenProvider, never()).generateAccessToken(any());
        verify(tokenProvider, never()).generateRefreshToken(any());
    }

    /**
     * 토큰 갱신 테스트
     * 
     * 시나리오: 유효한 리프레시 토큰으로 새 액세스 토큰을 발급받아야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("리프레시 토큰으로 새 액세스 토큰 발급")
    void refreshToken_Success() {
        // Given - 리프레시 토큰을 사용한 새 액세스 토큰 발급 테스트 준비
        String refreshToken = TEST_REFRESH_TOKEN;

        // 토큰 제공자인 tokenProvider가 리프레시 토큰에서 사용자 아이디를 추출하도록 설정
        when(tokenProvider.getUserIdFromToken(refreshToken)).thenReturn(TEST_USER_ID);

        // 토큰 제공자인 tokenProvider가 리프레시 토큰으로 인증 객체를 생성하도록 설정
        when(tokenProvider.getAuthentication(refreshToken)).thenReturn(authentication);

        // 토큰 제공자인 tokenProvider가 토큰 유효성을 검증하도록 설정
        when(tokenProvider.validateToken(refreshToken)).thenReturn(true);

        // 토큰 제공자인 tokenProvider가 새 액세스 토큰과 새 만료시간을 생성하도록 설정
        when(tokenProvider.generateAccessToken(authentication)).thenReturn(TEST_ACCESS_TOKEN);
        when(tokenProvider.getTokenExpirationTime()).thenReturn(TEST_EXPIRES_IN);

        // When - 테스트 실행
        TokenResponseDto result = authService.refreshToken(refreshToken);

        // Then - 결과 검증
        assertNotNull(result, "결과는 null이 아니어야 함");
        assertEquals(TEST_ACCESS_TOKEN, result.getAccessToken(), "새 액세스 토큰이 일치해야 함");
        assertEquals(refreshToken, result.getRefreshToken(), "리프레시 토큰이 유지되어야 함");

        // 토큰 제공자의 메소드들이 호출되었는지 검증
        verify(tokenProvider).validateToken(refreshToken);
        verify(tokenProvider).getUserIdFromToken(refreshToken);
        verify(tokenProvider).getAuthentication(refreshToken);
        verify(tokenProvider).generateAccessToken(authentication);
    }

    /**
     * 토큰 갱신 실패 테스트 - 유효하지 않은 리프레시 토큰
     * 
     * 시나리오: 유효하지 않은 리프레시 토큰으로 갱신 시도 시 예외가 발생해야 함
     */
    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 갱신 실패")
    void refreshToken_InvalidToken() {
        // Given - 테스트 준비
        String invalidRefreshToken = "invalid.refresh.token";

        // 토큰 제공자인 tokenProvider가 토큰 유효성 검증 실패를 반환하도록 설정
        when(tokenProvider.validateToken(invalidRefreshToken)).thenReturn(false);

        // When & Then - 테스트 실행 및 예외 검증
        assertThrows(ApplicationException.InvalidTokenException.class,
                () -> authService.refreshToken(invalidRefreshToken),
                "유효하지 않은 토큰으로 갱신 시 InvalidTokenException이 발생해야 함");

        // 토큰 제공자인 tokenProvider의 validateToken 메소드가 호출되었는지 검증
        verify(tokenProvider).validateToken(invalidRefreshToken);
        // 다른 메소드들은 호출되지 않았는지 검증
        verify(tokenProvider, never()).getUserIdFromToken(any());
        verify(tokenProvider, never()).getAuthentication(any());
        verify(tokenProvider, never()).generateAccessToken(any());
    }
}
