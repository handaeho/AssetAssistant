package kr.daeho.AssetAssistant.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import kr.daeho.AssetAssistant.auth.dto.LoginRequestDto;
import kr.daeho.AssetAssistant.auth.interfaces.AuthInterfaces;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;

/**
 * AuthController 단위 테스트 클래스 -> AuthController 클래스 내 메소드 테스트
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
public class AuthControllerTest {
    /**
     * 테스트 대상 객체
     * 
     * @InjectMocks: 모의 객체들을 주입받는 실제 객체
     */
    @InjectMocks
    private AuthController authController;

    /**
     * 모의 객체 - AuthInterfaces
     * 
     * 사용자 인증 관련 인터페이스
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private AuthInterfaces authInterfaces;

    /**
     * 테스트 데이터
     */
    private MockMvc mockMvc; // Spring MVC 모의 객체
    private ObjectMapper objectMapper; // JSON 변환을 위한 유틸리티 객체

    // AuthController 클래스와 동일한 토큰 관련 쿠키 상수 정의
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final String TEST_USER_ID = "testUser"; // 테스트 ID
    private final String TEST_PASSWORD = "testPassword123!"; // 테스트 비밀번호
    private final String TEST_DEVICE_ID = "testDevice"; // 테스트 디바이스 ID
    private final String TEST_ACCESS_TOKEN = "testAccessToken"; // 테스트 액세스 토큰
    private final String TEST_REFRESH_TOKEN = "testRefreshToken"; // 테스트 리프레시 토큰

    /**
     * 각 테스트 전에 실행되는 설정 메소드
     * 
     * @BeforeEach: 각 테스트 메소드 실행 전에 호출됨
     */
    @BeforeEach
    void setUp() {
        // 컨트롤러 단독 테스트를 위한 MockMvc 설정 (컨트롤러만 테스트하고 스프링 컨텍스트는 로드하지 않음)
        // MockMvc: 실제 서블릿 환경이나 네트워크 요청 없이도 컨트롤러, 서비스 로직을 테스트할 수 있도록 도와주는 가짜 구현체
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        // JSON 직렬화/역직렬화를 위한 ObjectMapper 초기화
        objectMapper = new ObjectMapper();
    }

    /**
     * 로그인 성공 테스트
     * 
     * 시나리오: 올바른 LoginRequestDto (사용자 아이디/비밀번호/디바이스 ID)로 로그인하면
     * 올바른 토큰정보 (사용자 ID/디바이스 ID/액세스 토큰/리프레시 토큰)를 반환해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccessTest() throws Exception {
        // Given - 로그인 테스트 준비
        // 로그인 요청 DTO 객체 생성 (빌더 패턴 사용)
        LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                .userId(TEST_USER_ID)
                .password(TEST_PASSWORD)
                .deviceId(TEST_DEVICE_ID)
                .build();

        // login 메소드가 반환할 토큰 정보를 담은 Map 객체 생성
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("userId", TEST_USER_ID); // 사용자 ID
        tokenInfo.put("deviceId", TEST_DEVICE_ID); // 디바이스 ID
        tokenInfo.put("accessToken", TEST_ACCESS_TOKEN); // 액세스 토큰
        tokenInfo.put("refreshToken", TEST_REFRESH_TOKEN); // 리프레시 토큰

        // login 메소드 호출 시 tokenInfo를 반환
        // any(LoginRequestDto.class)는 어떤 LoginRequestDto 객체가 전달되어도 매칭
        when(authInterfaces.login(any(LoginRequestDto.class))).thenReturn(tokenInfo);

        // When & Then (실행 및 검증): 요청 실행 및 결과 검증
        // mockMvc.perform()을 통해 "/api/auth/login" 엔드포인트에 대한 로그인 POST 요청 시뮬레이션
        mockMvc.perform(post("/api/auth/login")
                // 요청 본문 타입을 JSON으로 설정
                .contentType(MediaType.APPLICATION_JSON)
                // loginRequestDto 객체를 JSON 문자열로 변환하여 요청 본문에 설정
                .content(objectMapper.writeValueAsString(loginRequestDto)))
                // 응답의 HTTP 상태 코드가 200 OK인지 검증
                .andExpect(status().isOk())
                // 응답 JSON의 userId 필드 값이 TEST_USER_ID와 일치하는지 검증
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                // 응답 JSON의 deviceId 필드 값이 TEST_DEVICE_ID와 일치하는지 검증
                .andExpect(jsonPath("$.deviceId").value(TEST_DEVICE_ID))
                // 응답에 accessToken이라는 이름의 쿠키가 존재하는지 검증
                .andExpect(cookie().exists("accessToken"))
                // 응답에 refreshToken이라는 이름의 쿠키가 존재하는지 검증
                .andExpect(cookie().exists("refreshToken"));

        // authInterfaces.login 메소드가 LoginRequestDto으로 호출되었는지 검증
        verify(authInterfaces).login(any(LoginRequestDto.class));
    }

    /**
     * 토큰 갱신 성공 테스트
     * 
     * 시나리오: 올바른 리프레시 토큰으로 토큰 갱신 요청하면, 새로운 액세스 토큰을 반환해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("토큰 갱신 성공 테스트")
    void refreshTokenSuccessTest() throws Exception {
        // Given - 토큰 갱신 테스트 준비
        // refreshToken 메소드가 반환할 토큰 정보를 담은 Map 객체 생성
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("userId", TEST_USER_ID); // 사용자 ID
        tokenInfo.put("deviceId", TEST_DEVICE_ID); // 디바이스 ID
        tokenInfo.put("accessToken", "newAccessToken"); // 새로운 액세스 토큰
        tokenInfo.put("refreshToken", TEST_REFRESH_TOKEN); // 기존 리프레시 토큰

        // 리프레시 토큰으로 refreshToken 메소드 호출 시, tokenInfo를 반환하도록 모의 설정
        when(authInterfaces.refreshToken(TEST_REFRESH_TOKEN)).thenReturn(tokenInfo);

        // When & Then (실행 및 검증): 요청 실행 및 결과 검증
        // mockMvc.perform()을 통해 "/api/auth/refresh" 엔드포인트에 대한 토큰 갱신 POST 요청 시뮬레이션
        mockMvc.perform(post("/api/auth/refresh")
                // 요청에 리프레시 토큰이 포함된 쿠키 설정
                .cookie(new Cookie(REFRESH_TOKEN_COOKIE, TEST_REFRESH_TOKEN)))
                // 응답의 HTTP 상태 코드가 200 OK인지 검증
                .andExpect(status().isOk())
                // 응답에 ACCESS_TOKEN_COOKIE이라는 이름의 쿠키가 존재하는지 검증
                .andExpect(cookie().exists(ACCESS_TOKEN_COOKIE));

        // authInterfaces.refreshToken 메소드가 리프레시 토큰으로 호출되었는지 검증
        verify(authInterfaces).refreshToken(TEST_REFRESH_TOKEN);
    }

    /**
     * 리프레시 토큰 없이 토큰 갱신 실패 테스트
     * 
     * 시나리오: 리프레시 토큰이 없는 경우, 토큰 갱신 요청이 실패해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("리프레시 토큰 없이 토큰 갱신 실패 테스트")
    void refreshTokenWithoutRefreshTokenTest() throws Exception {
        // When & Then (실행 및 검증): 요청 실행 및 결과 검증
        // mockMvc.perform()을 통해 "/api/auth/refresh" 엔드포인트에 대한 토큰 갱신 POST 요청 시뮬레이션
        mockMvc.perform(post("/api/auth/refresh"))
                // 응답의 HTTP 상태 코드가 401 Unauthorized인지 검증
                .andExpect(status().isUnauthorized());

        // authInterfaces.refreshToken 메소드가 호출되지 않았는지 검증
        verify(authInterfaces, never()).refreshToken(anyString());
    }

    /**
     * 유효하지 않은 리프레시 토큰으로 토큰 갱신 실패 테스트
     * 
     * 시나리오: 유효하지 않은 리프레시 토큰으로 토큰 갱신 요청하면, 토큰 갱신 요청이 실패해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 리프레시 토큰")
    void refreshTokenWithInvalidRefreshTokenTest() throws Exception {
        // Given - 유효하지 않은 리프레시 토큰으로 토큰 갱신 준비 및 예외 발생
        when(authInterfaces.refreshToken("invalidToken"))
                .thenThrow(new ApplicationException.InvalidTokenException("유효하지 않은 리프레시 토큰"));

        // When & Then (실행 및 검증): 요청 실행 및 결과 검증
        // mockMvc.perform()을 통해 "/api/auth/refresh" 엔드포인트에 대한 토큰 갱신 POST 요청 시뮬레이션
        mockMvc.perform(post("/api/auth/refresh")
                // 유효하지 않은 리프레시 토큰 쿠키 추가
                .cookie(new Cookie(REFRESH_TOKEN_COOKIE, "invalidToken")))
                // 응답의 HTTP 상태 코드가 401 Unauthorized인지 검증
                .andExpect(status().isUnauthorized());

        // authInterfaces.refreshToken 메소드가 유효하지 않은 리프레시 토큰으로 호출되었는지 검증
        verify(authInterfaces).refreshToken("invalidToken");
    }

    /**
     * 로그아웃 성공 테스트 - 특정 디바이스
     * 
     * 시나리오: 올바른 액세스 토큰으로 로그아웃 요청하면, 로그아웃 요청이 성공해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("로그아웃 성공 테스트 - 특정 디바이스")
    void logoutSpecificDeviceSuccessTest() throws Exception {
        // Given - 로그아웃 테스트 준비
        // 올바른 액세스 토큰으로 validateToken 메소드 호출 시, 테스트 사용자 ID를 반환
        when(authInterfaces.validateToken(TEST_ACCESS_TOKEN)).thenReturn(TEST_USER_ID);

        // logout 메소드 호출 시, 테스트 사용자 ID와 테스트 디바이스 ID를 인자로 전달
        // doNothing(): void 메소드를 모킹 (logout)
        doNothing().when(authInterfaces).logout(TEST_USER_ID, TEST_DEVICE_ID);

        // When & Then (실행 및 검증): 요청 실행 및 결과 검증
        // mockMvc.perform()을 통해 "/api/auth/logout" 엔드포인트에 대한 로그아웃 POST 요청 시뮬레이션
        mockMvc.perform(post("/api/auth/logout")
                // 액세스 토큰 쿠키 추가
                .cookie(new Cookie(ACCESS_TOKEN_COOKIE, TEST_ACCESS_TOKEN))
                // 디바이스 ID 파라미터 추가
                .param("deviceId", TEST_DEVICE_ID))
                // 응답의 HTTP 상태 코드가 204 No Content인지 검증 (로그아웃 성공)
                .andExpect(status().isNoContent())
                // accessToken 쿠키가 0초 후 만료되어야 함 (즉시 만료)
                .andExpect(cookie().maxAge(ACCESS_TOKEN_COOKIE, 0))
                // refreshToken 쿠키가 0초 후 만료되어야 함 (즉시 만료)
                .andExpect(cookie().maxAge(REFRESH_TOKEN_COOKIE, 0));

        // authInterfaces.validateToken 메소드가 TEST_ACCESS_TOKEN으로 호출되었는지 검증
        verify(authInterfaces).validateToken(TEST_ACCESS_TOKEN);

        // authInterfaces.logout 메소드가 TEST_USER_ID와 TEST_DEVICE_ID으로 호출되었는지 검증
        verify(authInterfaces).logout(TEST_USER_ID, TEST_DEVICE_ID);
    }

    /**
     * 로그아웃 성공 테스트 - 모든 디바이스
     * 
     * 시나리오: 올바른 액세스 토큰으로 로그아웃 요청하면, 로그아웃 요청이 성공해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("로그아웃 성공 테스트 - 모든 디바이스")
    void logoutAllDevicesSuccessTest() throws Exception {
        // Given - 로그아웃 테스트 준비
        // 올바른 액세스 토큰으로 validateToken 메소드 호출 시, 테스트 사용자 ID를 반환
        when(authInterfaces.validateToken(TEST_ACCESS_TOKEN)).thenReturn(TEST_USER_ID);

        // logout 메소드 호출 시, 테스트 사용자 ID를 인자로 전달
        // doNothing(): void 메소드를 모킹 (logout)
        doNothing().when(authInterfaces).logout(TEST_USER_ID);

        // When & Then (실행 및 검증): 요청 실행 및 결과 검증
        // mockMvc.perform()을 통해 "/api/auth/logout" 엔드포인트에 대한 로그아웃 POST 요청 시뮬레이션
        mockMvc.perform(post("/api/auth/logout")
                // 액세스 토큰 쿠키 추가
                .cookie(new Cookie(ACCESS_TOKEN_COOKIE, TEST_ACCESS_TOKEN)))
                // 응답의 HTTP 상태 코드가 204 No Content인지 검증 (로그아웃 성공)
                .andExpect(status().isNoContent())
                // accessToken 쿠키가 0초 후 만료되어야 함 (즉시 만료)
                .andExpect(cookie().maxAge(ACCESS_TOKEN_COOKIE, 0))
                // refreshToken 쿠키가 0초 후 만료되어야 함 (즉시 만료)
                .andExpect(cookie().maxAge(REFRESH_TOKEN_COOKIE, 0));

        // authInterfaces.validateToken 메소드가 TEST_ACCESS_TOKEN으로 호출되었는지 검증
        verify(authInterfaces).validateToken(TEST_ACCESS_TOKEN);

        // authInterfaces.logout 메소드가 TEST_USER_ID으로 호출되었는지 검증
        verify(authInterfaces).logout(TEST_USER_ID);
    }
}
