package kr.daeho.AssetAssistant.users.service;

import kr.daeho.AssetAssistant.common.exception.ApplicationException;
import kr.daeho.AssetAssistant.users.dto.SignupRequestDto;
import kr.daeho.AssetAssistant.users.dto.UserDto;
import kr.daeho.AssetAssistant.users.entity.UserEntity;
import kr.daeho.AssetAssistant.users.repository.UserReposiory;
import kr.daeho.AssetAssistant.users.util.ModelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService 단위 테스트 클래스
 * 
 * 이 클래스는 사용자 서비스의 메소드들을 테스트합니다.
 * 
 * @ExtendWith(MockitoExtension.class): JUnit 5에서 Mockito 확장을 사용하도록 설정
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    /**
     * 테스트 대상 객체
     */
    @InjectMocks
    private UserService userService;

    /**
     * 모의 객체 - 사용자 리포지토리
     */
    @Mock
    private UserReposiory userRepository;

    /**
     * 모의 객체 - 비밀번호 인코더
     */
    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * 모의 객체 - 모델 매퍼
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
     */
    @BeforeEach
    void setUp() {
        // 테스트용 사용자 엔티티 생성
        testUserEntity = UserEntity.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .userName(TEST_USERNAME)
                .userPassword(TEST_ENCRYPTED_PASSWORD)
                .userAge(TEST_USER_AGE)
                .userJob(TEST_USER_JOB)
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .build();

        // 테스트용 사용자 DTO 생성
        testUserDto = UserDto.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .userName(TEST_USERNAME)
                .userAge(TEST_USER_AGE)
                .userJob(TEST_USER_JOB)
                .userUpdatedAt(LocalDateTime.now())
                .build();

        // 테스트용 회원가입 요청 DTO 생성
        testSignupRequestDto = SignupRequestDto.builder()
                .userId(TEST_USER_ID)
                .userName(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .userAge(TEST_USER_AGE)
                .userJob(TEST_USER_JOB)
                .build();
    }

    /**
     * 사용자 정보 조회 테스트
     * 
     * 시나리오: 존재하는 사용자 아이디로 조회하면 사용자 정보를 반환해야 함
     */
    @Test
    @DisplayName("사용자 정보 조회 성공")
    void getUserInfo_Success() {
        // Given - 테스트 준비
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(testUserEntity));
        when(modelMapper.toUserDto(testUserEntity)).thenReturn(testUserDto);

        // When - 테스트 실행
        UserDto result = userService.getUserInfo(TEST_USER_ID);

        // Then - 결과 검증
        assertNotNull(result, "결과는 null이 아니어야 함");
        assertEquals(TEST_USER_ID, result.getUserId(), "사용자 아이디가 일치해야 함");
        assertEquals(TEST_USERNAME, result.getUserName(), "사용자 이름이 일치해야 함");

        // 리포지토리의 findByUserId 메소드가 호출되었는지 검증
        verify(userRepository).findByUserId(TEST_USER_ID);
        // 모델 매퍼의 toUserDto 메소드가 호출되었는지 검증
        verify(modelMapper).toUserDto(testUserEntity);
    }

    /**
     * 사용자 정보 조회 실패 테스트 - 존재하지 않는 사용자
     * 
     * 시나리오: 존재하지 않는 사용자 아이디로 조회하면 예외가 발생해야 함
     */
    @Test
    @DisplayName("존재하지 않는 사용자 조회 실패")
    void getUserInfo_NotFound() {
        // Given - 테스트 준비
        String nonExistentUserId = "nonexistent";
        when(userRepository.findByUserId(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then - 테스트 실행 및 예외 검증
        assertThrows(ApplicationException.UserNotFoundException.class,
                () -> userService.getUserInfo(nonExistentUserId),
                "존재하지 않는 사용자 조회 시 UserNotFoundException이 발생해야 함");

        // 리포지토리의 findByUserId 메소드가 호출되었는지 검증
        verify(userRepository).findByUserId(nonExistentUserId);
        // 모델 매퍼의 toUserDto 메소드가 호출되지 않았는지 검증
        verify(modelMapper, never()).toUserDto(any());
    }

    /**
     * 회원가입 테스트
     * 
     * 시나리오: 유효한 회원가입 요청 시 사용자가 생성되고 정보가 반환되어야 함
     */
    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() {
        // Given - 테스트 준비
        // 사용자 아이디 중복 검사 (중복 없음)
        when(userRepository.existsByUserId(TEST_USER_ID)).thenReturn(false);
        // 비밀번호 암호화
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCRYPTED_PASSWORD);
        // 사용자 엔티티 저장
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);
        // DTO 변환
        when(modelMapper.toUserDto(testUserEntity)).thenReturn(testUserDto);

        // When - 테스트 실행
        UserDto result = userService.signup(testSignupRequestDto);

        // Then - 결과 검증
        assertNotNull(result, "결과는 null이 아니어야 함");
        assertEquals(TEST_USER_ID, result.getUserId(), "사용자 아이디가 일치해야 함");

        // 리포지토리의 메소드들이 호출되었는지 검증
        verify(userRepository).existsByUserId(TEST_USER_ID);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).save(any(UserEntity.class));
        verify(modelMapper).toUserDto(any(UserEntity.class));
    }

    /**
     * 회원가입 실패 테스트 - 중복된 사용자 아이디
     * 
     * 시나리오: 이미 존재하는 사용자 아이디로 회원가입 시도 시 예외가 발생해야 함
     */
    @Test
    @DisplayName("중복된 사용자 아이디로 회원가입 실패")
    void signup_DuplicateUserId() {
        // Given - 테스트 준비
        // 사용자 아이디 중복 검사 (중복 있음)
        when(userRepository.existsByUserId(TEST_USER_ID)).thenReturn(true);

        // When & Then - 테스트 실행 및 예외 검증
        assertThrows(ApplicationException.UserAlreadyExistsException.class,
                () -> userService.signup(testSignupRequestDto),
                "중복된 사용자 아이디로 회원가입 시 UserAlreadyExistsException이 발생해야 함");

        // 리포지토리의 existsByUserId 메소드가 호출되었는지 검증
        verify(userRepository).existsByUserId(TEST_USER_ID);
        // 다른 메소드들은 호출되지 않았는지 검증
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    /**
     * 비밀번호 변경 테스트
     * 
     * 시나리오: 올바른 현재 비밀번호를 제공하고 새 비밀번호로 변경 시 성공해야 함
     */
    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() {
        // Given - 테스트 준비
        String currentPassword = "CurrentPassword123!";
        String newPassword = "NewPassword456!";
        String encodedNewPassword = "encodedNewPassword";

        // 사용자 조회
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(testUserEntity));
        // 현재 비밀번호 일치 확인
        when(passwordEncoder.matches(currentPassword, testUserEntity.getUserPassword())).thenReturn(true);
        // 새 비밀번호 암호화
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        // When - 테스트 실행
        userService.changePassword(TEST_USER_ID, currentPassword, newPassword);

        // Then - 검증
        // 사용자 엔티티의 비밀번호가 업데이트되었는지 확인
        verify(userRepository).findByUserId(TEST_USER_ID);
        verify(passwordEncoder).matches(currentPassword, testUserEntity.getUserPassword());
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUserEntity);
    }

    /**
     * 비밀번호 변경 실패 테스트 - 현재 비밀번호 불일치
     * 
     * 시나리오: 잘못된 현재 비밀번호를 제공하면 예외가 발생해야 함
     */
    @Test
    @DisplayName("현재 비밀번호 불일치로 비밀번호 변경 실패")
    void changePassword_WrongCurrentPassword() {
        // Given - 테스트 준비
        String wrongCurrentPassword = "WrongPassword";
        String newPassword = "NewPassword456!";

        // 사용자 조회
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(testUserEntity));
        // 현재 비밀번호 불일치
        when(passwordEncoder.matches(wrongCurrentPassword, testUserEntity.getUserPassword())).thenReturn(false);

        // When & Then - 테스트 실행 및 예외 검증
        assertThrows(ApplicationException.UserPasswordNotMatchException.class,
                () -> userService.changePassword(TEST_USER_ID, wrongCurrentPassword, newPassword),
                "잘못된 현재 비밀번호로 변경 시 UserPasswordNotMatchException이 발생해야 함");

        // 검증
        verify(userRepository).findByUserId(TEST_USER_ID);
        verify(passwordEncoder).matches(wrongCurrentPassword, testUserEntity.getUserPassword());
        // 다른 메소드들은 호출되지 않았는지 검증
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}