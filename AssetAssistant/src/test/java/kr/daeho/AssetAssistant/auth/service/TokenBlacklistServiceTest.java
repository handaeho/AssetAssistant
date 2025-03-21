package kr.daeho.AssetAssistant.auth.service;

import java.util.concurrent.TimeUnit;
// Static Import: 클래스의 static 멤버(필드나 메서드)를 클래스 이름 없이 바로 사용
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * TokenBlacklistService 단위 테스트 클래스 -> TokenBlacklistService 클래스 내 메소드 테스트
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
public class TokenBlacklistServiceTest {
    /**
     * 테스트 대상 객체
     * 
     * @InjectMocks: 모의 객체들을 주입받는 실제 객체
     */
    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    /**
     * 모의 객체 - RedisTemplate
     * 
     * RedisTemplate: Redis 연결 및 데이터 조작을 위한 템플릿 클래스
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 모의 객체 - ValueOperations
     * 
     * ValueOperations: Redis 문자열 값과 상호작용하는 방식을 단순화 하는 객체
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private ValueOperations<String, String> valueOperations;

    /**
     * 테스트 데이터
     */
    private final String TEST_TOKEN = "testToken";
    private final long TEST_TTL = 3600L; // 1시간
    private final String BLACKLIST_PREFIX = "blacklist:token:";

    /**
     * 토큰 블랙리스트 추가 테스트
     * 
     * 시나리오: 토큰을 블랙리스트에 추가하면 해당 토큰은 더 이상 유효하지 않음
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("토큰 블랙리스트 추가 테스트")
    void addToBlacklistTest() {
        // Given - 토큰 블랙리스트 추가 테스트 준비
        // 블랙리스트 키 (프리픽스 + 토큰 값)
        String blacklistKey = BLACKLIST_PREFIX + TEST_TOKEN;

        // redisTemplate의 opsForValue() 메소드가 호출될 때, valueOperations 객체를 반환
        // addToBlacklist() 메소드 내에서 redisTemplate.opsForValue() 메소드 호출
        // -> redisTemplate.opsForValue().set(blacklistKey, "true");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When - 토큰을 블랙리스트에 추가하는 메소드 호출
        tokenBlacklistService.addToBlacklist(TEST_TOKEN, TEST_TTL);

        // Then - 블랙리스트 키 설정 여부 및 만료 시간 검증
        verify(valueOperations).set(blacklistKey, "true");
        verify(redisTemplate).expire(blacklistKey, TEST_TTL, TimeUnit.SECONDS);
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인 테스트 - 있음
     * 
     * 시나리오: 블랙리스트 토큰이 블랙리스트에 존재하는지 확인
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("토큰이 블랙리스트에 있는지 확인 테스트 - 있음")
    void isBlacklistedTrueTest() {
        // Given - 토큰 블랙리스트 확인 테스트 준비
        // 블랙리스트 키 (프리픽스 + 토큰 값)
        String blacklistKey = BLACKLIST_PREFIX + TEST_TOKEN;

        // 블랙리스트 토큰이 Redis 저장소에 존재하는 경우
        when(redisTemplate.hasKey(blacklistKey)).thenReturn(true);

        // When - 토큰이 블랙리스트에 있는지 확인하는 메소드 호출
        boolean result = tokenBlacklistService.isBlacklisted(TEST_TOKEN);

        // Then - 토큰이 블랙리스트에 있는지 확인하는 메소드 검증
        assertTrue(result);

        // Redis 저장소에 대한 확인 검증
        verify(redisTemplate).hasKey(blacklistKey);
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인 테스트 - 없음
     * 
     * 시나리오: 블랙리스트 토큰이 블랙리스트에 존재하지 않는지 확인
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("토큰이 블랙리스트에 있는지 확인 테스트 - 없음")
    void isBlacklistedFalseTest() {
        // Given - 토큰 블랙리스트 확인 테스트 준비
        // 블랙리스트 키 (프리픽스 + 토큰 값)
        String blacklistKey = BLACKLIST_PREFIX + TEST_TOKEN;

        // 블랙리스트 토큰이 Redis 저장소에 존재하지 않는 경우
        when(redisTemplate.hasKey(blacklistKey)).thenReturn(false);

        // When - 토큰이 블랙리스트에 있는지 확인하는 메소드 호출
        boolean result = tokenBlacklistService.isBlacklisted(TEST_TOKEN);

        // Then - 토큰이 블랙리스트에 있는지 확인하는 메소드 검증
        assertFalse(result);

        // Redis 저장소에 대한 확인 검증
        verify(redisTemplate).hasKey(blacklistKey);
    }

    /**
     * 여러 토큰을 블랙리스트에 추가 테스트
     * 
     * 시나리오: 여러 토큰을 블랙리스트에 추가
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("여러 토큰을 블랙리스트에 추가 테스트")
    void addAllToBlacklistTest() {
        // Given - 여러 토큰을 블랙리스트에 추가 테스트 준비
        String[] tokens = { "token1", "token2", "token3" };

        // redisTemplate의 opsForValue() 메소드가 호출될 때, valueOperations 객체를 반환
        // addAllToBlacklist() 메소드 내에서 redisTemplate.opsForValue() 메소드 호출
        // -> redisTemplate.opsForValue().set(blacklistKey, "true");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When - 여러 토큰을 블랙리스트에 추가하는 메소드 호출
        tokenBlacklistService.addAllToBlacklist(tokens, TEST_TTL);

        // Then - 여러 토큰을 블랙리스트에 추가하는 메소드 검증
        // valueOperations가 블랙리스트에 추가하는 토큰 수만큼 호출되어야 함
        verify(valueOperations, times(3)).set(anyString(), eq("true"));
        // redisTemplate이 블랙리스트에 추가하는 토큰 수만큼 호출되어야 함
        verify(redisTemplate, times(3)).expire(anyString(), eq(TEST_TTL), eq(TimeUnit.SECONDS));
    }
}
