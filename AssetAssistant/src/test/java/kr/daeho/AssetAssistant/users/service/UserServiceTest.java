package kr.daeho.AssetAssistant.users.service;

import java.time.LocalDateTime;
import java.util.Optional;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import kr.daeho.AssetAssistant.common.exception.ApplicationException;
import kr.daeho.AssetAssistant.common.utils.ModelMapper;
import kr.daeho.AssetAssistant.users.dto.SignupRequestDto;
import kr.daeho.AssetAssistant.users.dto.UserDto;
import kr.daeho.AssetAssistant.users.entity.UserEntity;
import kr.daeho.AssetAssistant.users.repository.UserReposiory;
import kr.daeho.AssetAssistant.users.enums.UserRoleEnum;

/**
 * UserService 단위 테스트 클래스 -> UserService 클래스 내 메소드 테스트
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
public class UserServiceTest {
    /**
     * 테스트 대상 객체 - 사용자 서비스 (사용자 정보 조회, 수정, 삭제, 비밀번호 변경)
     * 
     * @InjectMocks: 모의 객체들을 주입받는 실제 객체
     */
    @InjectMocks
    private UserService userService;

    /**
     * 테스트 대상 객체 - 사용자 서비스 (사용자 회원가입)
     * 
     * @InjectMocks: 모의 객체들을 주입받는 실제 객체
     */
    @InjectMocks
    private UserSignupService userSignupService;

    /**
     * 모의 객체 - 사용자 리포지토리
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private UserReposiory userRepository;

    /**
     * 모의 객체 - 비밀번호 인코더 (사용자 입력 비밀번호 암호화)
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * 모의 객체 - 모델 매퍼(dto <-> entity 변환)
     * 
     * @Mock: 실제 동작을 흉내내는 가짜 객체
     */
    @Mock
    private ModelMapper modelMapper;

    /**
     * 테스트 데이터
     */
    private final String TEST_USER_ID = "testuser";
    private final String TEST_USERNAME = "테스트 사용자";
    private final String TEST_PASSWORD = "Password123!";
    private final String TEST_ENCRYPTED_PASSWORD = "encryptedPassword";
    private final int TEST_USER_AGE = 30;
    private final String TEST_USER_JOB = "개발자";
    private UserEntity testUserEntity;
    private UserDto testUserDto;
    private SignupRequestDto testSignupRequestDto;

    /**
     * 각 테스트 전에 실행되는 설정 메소드
     * 
     * @BeforeEach: 각 테스트 메소드 실행 전에 호출됨
     */
    @BeforeEach
    void setUp() {
        // 테스트용 사용자 Entity 생성
        testUserEntity = UserEntity.builder()
                .userId(TEST_USER_ID) // 사용자 아이디
                .userName(TEST_USERNAME) // 사용자 이름
                .userPassword(TEST_ENCRYPTED_PASSWORD) // 사용자 비밀번호
                .userAge(TEST_USER_AGE) // 사용자 나이
                .userJob(TEST_USER_JOB) // 사용자 직업
                .userUpdatedAt(LocalDateTime.now()) // 사용자 정보 수정일
                .role(UserRoleEnum.ROLE_USER) // 사용자 역할
                .build();

        // 테스트용 사용자 DTO 생성
        testUserDto = UserDto.builder()
                .userId(TEST_USER_ID) // 사용자 아이디
                .userName(TEST_USERNAME) // 사용자 이름
                .userAge(TEST_USER_AGE) // 사용자 나이
                .userJob(TEST_USER_JOB) // 사용자 직업
                .userUpdatedAt(LocalDateTime.now()) // 사용자 정보 수정일
                .build();

        // 테스트용 회원가입 요청 DTO 생성
        testSignupRequestDto = SignupRequestDto.builder()
                .userId(TEST_USER_ID) // 사용자 아이디
                .userName(TEST_USERNAME) // 사용자 이름
                .password(TEST_PASSWORD) // 사용자 비밀번호
                .build();
    }

    /**
     * 사용자 정보 조회 테스트
     * 
     * 시나리오: 존재하는 사용자 아이디로 조회하면 사용자 정보를 반환해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("사용자 정보 조회 성공")
    void getUserInfo_Success() {
        // Given - 시용자 정보 조회 테스트 준비
        // 사용자 아이디로 레파지토리에서 사용자 정보 검색 후, 사용자 Entity 반환
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(testUserEntity));
        // modelMapper를 사용하여 사용자 Entity를 사용자 DTO로 변환 후 반환
        when(modelMapper.toUserDto(testUserEntity)).thenReturn(testUserDto);

        // When - 사용자 정보 조회 실행
        // 사용자 정보 조회 서비스를 호출하고, 사용자 아이디에 해당하는 사용자 정보 DTO 반환
        UserDto userDto = userService.getUserInfo(TEST_USER_ID);

        // Then - 사용자 정보 조회 테스트 결과 검증
        assertNotNull(userDto, "사용자 정보 조회 성공시, userDto가 NULL이 아님.");
        assertEquals(TEST_USER_ID, userDto.getUserId(), "사용자 정보 조회 성공시, 사용자 아이디가 일치함.");
        assertEquals(TEST_USERNAME, userDto.getUserName(), "사용자 정보 조회 성공시, 사용자 이름이 일치함.");
        assertEquals(TEST_USER_AGE, userDto.getUserAge(), "사용자 정보 조회 성공시, 사용자 나이가 일치함.");
        assertEquals(TEST_USER_JOB, userDto.getUserJob(), "사용자 정보 조회 성공시, 사용자 직업이 일치함.");

        // 리포지토리의 findByUserId 메소드가 호출되었는지 검증
        verify(userRepository).findByUserId(TEST_USER_ID);
        // 모델 매퍼의 toUserDto 메소드가 호출되었는지 검증
        verify(modelMapper).toUserDto(testUserEntity);
    }

    /**
     * 사용자 정보 조회 실패 테스트 - 존재하지 않는 사용자
     * 
     * 시나리오: 존재하지 않는 사용자 아이디로 조회하면 예외가 발생해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("존재하지 않는 사용자 조회 실패")
    void getUserInfo_NotFound() {
        // Given - 존재하지 않는 사용자 조회 테스트 준비
        String nonExistentUserId = "nonExistentUserId";
        when(userRepository.findByUserId(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then - 테스트 실행 및 예외 검증
        assertThrows(ApplicationException.UserNotFoundException.class,
                () -> userService.getUserInfo(nonExistentUserId),
                "존재하지 않는 사용자 조회 시, UserNotFoundException이 발생해야 함");

        // 리포지토리의 findByUserId 메소드가 호출되었는지 검증
        verify(userRepository).findByUserId(nonExistentUserId);
        // 모델 매퍼의 toUserDto 메소드가 호출되지 않았는지 검증
        verify(modelMapper, never()).toUserDto(any());
    }

    /**
     * 회원가입 테스트
     * 
     * 시나리오: 유효한 회원가입 요청 시 사용자가 생성되고 정보가 반환되어야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() {
        // Given - 회원가입 테스트 준비
        // 사용자 아이디 중복 검사 (중복 없음)
        when(userRepository.existsByUserId(TEST_USER_ID)).thenReturn(false);
        // 비밀번호 암호화
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCRYPTED_PASSWORD);
        // 사용자 Entity를 DB에 저장 후, 저장된 사용자 Entity 반환
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);
        // 사용자 Entity를 DTO로 변환 후, 변환된 사용자 DTO 반환
        when(modelMapper.toUserDto(testUserEntity)).thenReturn(testUserDto);

        // When - 회원가입 테스트 실행
        UserDto userDto = userSignupService.signup(testSignupRequestDto);

        // Then - 회원가입 결과 검증
        assertNotNull(userDto, "회원가입 성공시, userDto가 NULL이 아님.");
        assertEquals(TEST_USER_ID, userDto.getUserId(), "회원가입 성공시, 사용자 아이디가 일치함.");

        // 회원가입 진행 중, 필요한 메소드들이 호출되었는지 검증
        verify(userRepository).existsByUserId(TEST_USER_ID); // 사용자 아이디 중복 검사 수행
        verify(passwordEncoder).encode(TEST_PASSWORD); // 비밀번호 암호화 수행
        verify(userRepository).save(any(UserEntity.class)); // 사용자 Entity를 DB에 저장 수행
        verify(modelMapper).toUserDto(any(UserEntity.class)); // 사용자 Entity를 DTO로 변환 수행
    }

    /**
     * 회원가입 실패 테스트 - 중복된 사용자 아이디
     * 
     * 시나리오: 이미 존재하는 사용자 아이디로 회원가입 시도 시 예외가 발생해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("중복된 사용자 아이디로 회원가입 실패")
    void signup_DuplicateUserId() {
        // Given - 회원가입 실패 테스트 준비
        // 사용자 아이디 중복 검사 (중복 있음)
        when(userRepository.existsByUserId(TEST_USERNAME)).thenReturn(true);

        // When & Then - 회원가입 실패 테스트 실행 및 예외 검증
        assertThrows(ApplicationException.UserAlreadyExistsException.class,
                () -> userSignupService.signup(testSignupRequestDto),
                "중복된 사용자 아이디로 회원가입 시 UserAlreadyExistsException이 발생해야 함");


        // 리포지토리의 existsByUserId 메소드가 호출되었는지 검증
        verify(userRepository).existsByUserId(TEST_USER_ID); // 사용자 아이디 중복 검사 수행

        // 다른 메소드들은 호출되지 않았는지 검증
        verify(modelMapper, never()).toUserDto(any()); // 사용자 DTO 변환 수행 안됨
        verify(passwordEncoder, never()).encode(any()); // 비밀번호 암호화 수행 안됨
        verify(userRepository, never()).save(any()); // 사용자 엔티티 저장 수행 안됨
    }

    /**
     * 비밀번호 변경 테스트
     * 
     * 시나리오: 올바른 현재 비밀번호를 제공하고 새 비밀번호로 변경 시 성공해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */
    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() {
        // Given - 비밀번호 변경 테스트 준비
        String currentPassword = "CurrentPassword123!";
        String newPassword = "NewPassword456!";

        // 사용자 조회
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(testUserEntity));
        
        // 현재 비밀번호 일치 확인
        when(passwordEncoder.matches(currentPassword, testUserEntity.getUserPassword())).thenReturn(true);

        // When - 비밀번호 변경 테스트 실행 (userService에서 새 비밀번호 암호화 후 저장 수행)
        userService.changePassword(TEST_USER_ID, currentPassword, newPassword);

        // Then - 검증
        // 사용자 엔티티의 비밀번호가 업데이트되었는지 확인
        verify(userRepository).findByUserId(TEST_USER_ID); // 사용자 조회 수행
        verify(passwordEncoder).matches(currentPassword, testUserEntity.getUserPassword()); // 현재 비밀번호 일치 확인 수행
        verify(passwordEncoder).encode(newPassword); // 새 비밀번호 암호화 수행
        verify(userRepository).save(testUserEntity); // 사용자 엔티티 저장 수행
    }

    /**
     * 비밀번호 변경 실패 테스트 - 현재 비밀번호 불일치
     * 
     * 시나리오: 잘못된 현재 비밀번호를 제공하면 예외가 발생해야 함
     * 
     * @Test: 테스트 메소드 표시
     * @DisplayName: 테스트 목적을 명시적으로 표시 및 출력
     */

    @Test
    @DisplayName("현재 비밀번호 불일치로 비밀번호 변경 실패")
    void changePassword_WrongCurrentPassword() {
        // Given - 비밀번호 변경 실패 테스트 준비
        String wrongCurrentPassword = "WrongPassword123!";
        String newPassword = "NewPassword456!";

        // 사용자 조회
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(testUserEntity));

        // 현재 비밀번호 불일치
        when(passwordEncoder.matches(wrongCurrentPassword, testUserEntity.getUserPassword())).thenReturn(false);


        // When & Then - 테스트 실행 및 예외 검증
        // When & Then - 테스트 실행 및 예외 검증
        assertThrows(ApplicationException.UserPasswordNotMatchException.class,
                () -> userService.changePassword(TEST_USER_ID, wrongCurrentPassword, newPassword),
                "잘못된 현재 비밀번호로 변경 시 UserPasswordNotMatchException이 발생해야 함");


        // 사용자 정보 조회가 정상적으로 완료되었는지 검증
        verify(userRepository).findByUserId(TEST_USER_ID);
        // 저장된 암호화 비밀번호와 입력한 비밀번호가 일치하는지 검증
        verify(passwordEncoder).matches(wrongCurrentPassword, testUserEntity.getUserPassword());
        // 다른 메소드들은 호출되지 않았는지 검증
        verify(passwordEncoder, never()).encode(any()); // 새 비밀번호 암호화 수행 안됨
        verify(userRepository, never()).save(any()); // 사용자 엔티티 저장 수행 안됨
    }
}
