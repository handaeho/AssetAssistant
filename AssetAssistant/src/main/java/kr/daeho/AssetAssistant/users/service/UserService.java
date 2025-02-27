package kr.daeho.AssetAssistant.users.service;

import java.time.LocalDateTime;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.daeho.AssetAssistant.auth.dto.SignUpRequestDto;
import kr.daeho.AssetAssistant.auth.entity.AuthEntity;
import kr.daeho.AssetAssistant.auth.repository.AuthRepository;
import kr.daeho.AssetAssistant.common.exception.ApplicationExceptions;
import kr.daeho.AssetAssistant.common.util.ModelMapper;
import kr.daeho.AssetAssistant.users.dto.UserDto;
import kr.daeho.AssetAssistant.users.entity.UserEntity;
import kr.daeho.AssetAssistant.users.interfaces.UserInterfaces;
import kr.daeho.AssetAssistant.users.repository.UserReposiory;

/**
 * 사용자 관리 서비스 -> 사용자 등록(회원가입), 조회, 수정, 삭제 기능 담당
 * 
 * 사용자 정보를 조회, 등록, 수정, 삭제하는 기능을 제공
 * 
 * 컨트롤러에서 요청을 받아 비즈니스 로직을 처리하고, 결과를 반환
 * 
 * 핵심 비즈니스 로직 처리, 트랜잭션 관리, 예외 처리 등 웹 요청 및 응답을 위한 실제 로직 처리
 * 
 * 인터페이스를 상속받아 서비스를 구현(implements)함으로써,
 * 
 * 컨트롤러는 서비스(실제 구현체)가 아닌 인터페이스(계약)에 의존하여 의존성 역전 및 느슨한 결합 확보
 * 
 * @Service: 서비스 클래스임을 명시
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션 (@Autowired 대신 사용)
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserInterfaces {
    // final로 선언해 불변성 보장, @RequiredArgsConstructor로 생성자 자동 생성 및 의존성 주입
    private final UserReposiory userRepository; // 사용자 정보 저장을 위한 리포지토리
    private final AuthRepository authRepository; // 인증 정보 저장을 위한 리포지토리
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 및 일치 확인 등
    private final ModelMapper modelMapper; // DTO와 Entity 간 변환 처리

    /**
     * 회원가입 처리 -> 사용자 기본 정보와 인증 정보를 함께 저장
     * 
     * @param signUpRequestDto 회원가입 요청 정보
     * @throws ApplicationExceptions.UserAlreadyExistsException 아이디 중복 시
     */
    @Transactional
    public UserDto signUp(SignUpRequestDto signUpRequestDto) {
        log.info("회원가입 처리: {}", signUpRequestDto.getUserId());

        String userId = signUpRequestDto.getUserId();

        // 아이디 중복 검사 - 중복 시 UserAlreadyExistsException 발생
        if (authRepository.existsByUserId(userId)) {
            log.warn("아이디 중복: {}", userId);
            throw new ApplicationExceptions.UserAlreadyExistsException(userId);
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getPassword());

        try {
            // 1. 인증 정보 저장 (비밀번호 관련 정보는 AuthEntity에만 저장)
            // ModelMapper를 사용하여 SignUpRequestDto → AuthEntity 변환
            AuthEntity authEntity = modelMapper.signUpRequestToAuthEntity(signUpRequestDto, encodedPassword);
            authRepository.save(authEntity);

            // 2. 사용자 프로필 정보 저장 (UserEntity에는 비밀번호 없음)
            // ModelMapper를 사용하여 SignUpRequestDto → UserEntity 변환
            UserEntity userEntity = modelMapper.signUpRequestToUserEntity(signUpRequestDto);
            userRepository.save(userEntity);

            log.info("회원가입 완료: {}", userId);

            // ModelMapper를 사용하여 UserEntity → UserDto 변환 및 리턴
            return modelMapper.toUserDto(userEntity);
        } catch (Exception e) {
            // DB 저장 실패 등 기술적 예외 발생 시
            log.error("회원가입 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new ApplicationExceptions("USER_REGISTRATION_FAILED",
                    "회원가입 처리 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 사용자 정보 조회
     * 
     * @param userId 사용자 아이디
     * @return 사용자 정보 DTO
     * @throws ApplicationExceptions.UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    public UserDto getUserInfo(String userId) {
        log.info("사용자 정보 조회: {}", userId);

        // 사용자 정보 조회 - 없으면 UserNotFoundException 발생
        UserEntity userEntity = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음: {}", userId);
                    return new ApplicationExceptions.UserNotFoundException(userId);
                });

        // Entity를 DTO로 변환하여 반환
        return modelMapper.toUserDto(userEntity);
    }

    /**
     * 사용자 정보 수정
     * 
     * @param userId  사용자 아이디
     * @param userDto 수정할 사용자 정보
     * @return 수정된 사용자 정보 DTO
     * @throws ApplicationExceptions.UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public UserDto updateUser(String userId, UserDto userDto) {
        log.info("사용자 정보 수정: {}", userId);

        // 사용자 정보 조회 - 없으면 UserNotFoundException 발생
        UserEntity userEntity = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationExceptions.UserNotFoundException(userId));

        // ModelMapper를 사용해 엔티티 업데이트 (null이 아닌 필드만)
        modelMapper.updateUserEntityFromDto(userEntity, userDto);
        userRepository.save(userEntity);

        return modelMapper.toUserDto(userEntity);
    }

    /**
     * 사용자 삭제
     * 
     * @param userId 삭제할 사용자 아이디
     * @throws ApplicationExceptions.UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public void deleteUser(String userId) {
        log.info("사용자 삭제: {}", userId);

        // 사용자 존재 확인 - 없으면 UserNotFoundException 발생
        if (!userRepository.existsByUserId(userId)) {
            log.warn("사용자를 찾을 수 없음: {}", userId);
            throw new ApplicationExceptions.UserNotFoundException(userId);
        }

        try {
            // 인증 정보 삭제
            authRepository.deleteByUserId(userId);

            // 사용자 정보 삭제
            userRepository.deleteByUserId(userId);

            log.info("사용자 삭제 완료: {}", userId);
        } catch (Exception e) {
            // DB 삭제 실패 등 기술적 예외 발생 시
            log.error("사용자 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new ApplicationExceptions("USER_DELETE_FAILED",
                    "사용자 삭제 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 비밀번호 변경
     * 
     * @param userId          사용자 아이디
     * @param currentPassword 현재 비밀번호
     * @param newPassword     새 비밀번호
     * @throws ApplicationExceptions.UserPasswordNotMatchException 현재 비밀번호가 일치하지 않음
     * @throws ApplicationExceptions.UserNotFoundException         사용자를 찾을 수 없는 경우
     */
    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        log.info("비밀번호 변경 요청: {}", userId);

        // 인증 정보 조회 - 없으면 UserNotFoundException 발생
        AuthEntity authEntity = authRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음: {}", userId);
                    return new ApplicationExceptions.UserNotFoundException(userId);
                });

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, authEntity.getUserPassword())) {
            log.warn("비밀번호 불일치: {}", userId);
            throw new ApplicationExceptions.UserPasswordNotMatchException(userId);
        }

        try {
            // 새 비밀번호 암호화 및 저장
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            authEntity.updatePassword(encodedNewPassword);
            authRepository.save(authEntity);

            log.info("비밀번호 변경 완료: {}", userId);
        } catch (Exception e) {
            log.error("비밀번호 변경 중 오류 발생: {}", e.getMessage(), e);
            throw new ApplicationExceptions("PASSWORD_UPDATE_FAILED",
                    "비밀번호 변경 중 오류가 발생했습니다", e);
        }
    }
}
