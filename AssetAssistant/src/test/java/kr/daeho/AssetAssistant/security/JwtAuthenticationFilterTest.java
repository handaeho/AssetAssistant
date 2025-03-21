package kr.daeho.AssetAssistant.security;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
// Static Import: 클래스의 static 멤버(필드나 메서드)를 클래스 이름 없이 바로 사용
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import kr.daeho.AssetAssistant.auth.interfaces.AuthInterfaces;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;

/**
 * JwtAuthenticationFilter 단위 테스트 클래스 -> JwtAuthenticationFilter 클래스 내 메소드 테스트
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
public class JwtAuthenticationFilterTest {
    /**
     * 테스트 대상 객체
     * 
     * @InjectMocks: 모의 객체들을 주입받는 실제 객체
     */
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 모의 객체 - 인증 관리자
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private AuthInterfaces authInterfaces;

    /**
     * 테스트 데이터
     */
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;
    private final String TEST_USER_ID = "testUser";
    private final String TEST_ACCESS_TOKEN = "validAccessToken";
    private final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";

    /**
     * 각 테스트 전에 실행되는 설정 메소드
     * 
     * @BeforeEach: 각 테스트 메소드 실행 전에 호출됨
     */
    @BeforeEach
    void setUp() {
        // 테스트용 요청 객체 생성
        request = new MockHttpServletRequest();

        // 테스트용 응답 객체 생성
        response = new MockHttpServletResponse();

        // 테스트용 필터 체인 객체 생성
        filterChain = new MockFilterChain();

        // 사용자 인증 정보를 저장하는 SecurityContextHolder 초기화
        SecurityContextHolder.clearContext();
    }

    /**
     * 유효한 토큰으로 필터 통과 테스트
     * 
     * 시나리오: 유효한 토큰으로 필터 통과 시, 인증 정보가 저장된 SecurityContextHolder에 사용자 아이디 저장
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("유효한 토큰으로 필터 통과 테스트")
    void doFilterInternalWithValidTokenTest() throws ServletException, IOException {
        // Given - 유효한 토큰으로 필터 통과 테스트 준비
        // 토큰 name과 토큰 value로 쿠키 생성
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE, TEST_ACCESS_TOKEN);

        // 클라이언트 요청에 쿠키 추가
        request.setCookies(cookie);

        // 토큰 검증 후, 사용자 아이디 반환
        when(authInterfaces.validateToken(TEST_ACCESS_TOKEN)).thenReturn(TEST_USER_ID);

        // When - 유효한 토큰이 담긴 클라이언트 요청, 응답 생성 객체로 필터 통과 테스트 실행
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then - 유효한 토큰 필터 통과 테스트 검증
        // 토큰 검증 메서드 호출 검증
        verify(authInterfaces).validateToken(TEST_ACCESS_TOKEN);

        // 인증 정보가 저장된 SecurityContextHolder 검증
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(),
                "인증 정보가 저장된 SecurityContextHolder가 NULL이 아님.");
        assertEquals(TEST_USER_ID, SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                "인증 정보가 저장된 SecurityContextHolder에 저장된 사용자 아이디가 일치함.");
    }

    /**
     * 토큰 없이 필터 통과 테스트
     * 
     * 시나리오: 토큰 없이 필터 통과시, 인증 정보가 저장된 SecurityContextHolder가 NULL이어야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("토큰 없이 필터 통과 테스트")
    void doFilterInternalWithoutTokenTest() throws ServletException, IOException {
        // Given - 쿠키 없음

        // When - 토큰 없이 클라이언트 요청, 응답 생성 객체로 필터 통과 테스트 실행
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then - 토큰 없이 필터 통과 테스트 검증
        // 토큰 검증 메서드 호출이 되지 않음
        verify(authInterfaces, never()).validateToken(anyString());

        // 인증 정보가 저장된 SecurityContextHolder가 NULL
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "인증 정보가 저장된 SecurityContextHolder가 NULL임.");
    }

    /**
     * 유효하지 않은 토큰으로 필터 통과 테스트
     * 
     * 시나리오: 유효하지 않은 토큰으로 필터 통과시,
     * 예외가 발생하고, 인증 정보가 저장된 SecurityContextHolder가 NULL이어야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("유효하지 않은 토큰으로 필터 통과 테스트")
    void doFilterInternalWithInvalidTokenTest() throws ServletException, IOException {
        // Given - 유효하지 않은 토큰으로 필터 통과 테스트 준비
        // 유효하지 않은 쿠키 생성
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE, "invalidToken");

        // 클라이언트 요청에 유효하지 않은 쿠키 추가
        request.setCookies(cookie);

        // 유효하지 않은 토큰으로 토큰 검증 메서드 호출 시, 예외 발생
        when(authInterfaces.validateToken("invalidToken"))
                .thenThrow(new ApplicationException.InvalidTokenException("유효하지 않은 토큰"));

        // When - 유효하지 않은 토큰으로 클라이언트 요청, 응답 생성 객체로 필터 통과 테스트 실행
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then - 유효하지 않은 토큰으로 필터 통과 테스트 검증
        // 토큰 검증 메서드 호출 검증
        verify(authInterfaces).validateToken("invalidToken");

        // 인증 정보가 저장된 SecurityContextHolder가 NULL
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "인증 정보가 저장된 SecurityContextHolder가 NULL임.");
    }

    /**
     * 인증이 필요없는 URL 테스트
     * 
     * 시나리오:
     * 인증이 필요없는 URL 요청시, 필터를 통과해야 함
     * 인증이 필요한 URL 요청시, 필터를 통과하지 않아야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("인증 제외 URL 테스트")
    void shouldNotFilterTest() {
        // Given - 인증이 필요 없는 URL 설정
        request.setRequestURI("/api/auth/login");

        // When - 인증이 필요 없는 URL 요청시, 필터 통과 테스트 실행
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then - 인증이 필요 없는 URL 요청시, 필터 통과 테스트 검증
        assertTrue(result);

        // Given - 인증이 필요한 URL 설정
        request.setRequestURI("/api/users/profile");

        // When - 인증이 필요한 URL 요청시, 필터 통과 테스트 실행
        result = jwtAuthenticationFilter.shouldNotFilter(request);

        // Then - 인증이 필요한 URL 요청시, 필터 통과 테스트 검증
        assertFalse(result);
    }
}
