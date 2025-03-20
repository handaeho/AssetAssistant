package kr.daeho.AssetAssistant.users.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import kr.daeho.AssetAssistant.users.dto.UserDto;
import kr.daeho.AssetAssistant.users.entity.UserEntity;
import kr.daeho.AssetAssistant.users.interfaces.UserInterfaces;
import kr.daeho.AssetAssistant.users.repository.UserRepository;
import kr.daeho.AssetAssistant.common.utils.ModelMapper;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;

/**
 * 사용자 관리 서비스
 * 
 * 사용자 정보를 조회, 수정, 삭제, 비밀번호 변경하는 기능을 제공
 * 
 * 컨트롤러에서 요청을 받아 비즈니스 로직을 처리하고, 결과를 반환
 * 
 * 핵심 비즈니스 로직 처리, 트랜잭션 관리, 예외 처리 등 웹 요청 및 응답을 위한 실제 로직 처리
 * 
 * 인터페이스를 상속받아 서비스를 구현(implements)함으로써,
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
    private final UserRepository userRepository; // 사용자 정보 저장을 위한 리포지토리
    private final UserSecurityService userSecurityService; // 사용자 보안 관련 로직을 위한 서비스
    private final ModelMapper modelMapper; // 엔티티와 DTO 간 변환을 위한 모델 매퍼

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
                    return new ApplicationException.UserNotFoundException(userId);
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
        log.info("사용자 정보 수정 요청: {}", userId);

        // 사용자 정보 조회 - 없으면 UserNotFoundException 발생
        UserEntity userEntity = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException.UserNotFoundException(userId));

        // ModelMapper를 사용해 엔티티 업데이트 (null이 아닌 필드만)
        modelMapper.updateUserEntityFromDto(userEntity, userDto);

        // 저장
        userRepository.save(userEntity);

        log.info("사용자 정보 수정 완료: {}", userId);

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
            throw new ApplicationException.UserNotFoundException(userId);
        }

        try {
            // 인증 정보 삭제
            // authRepository.deleteByUserId(userId);

            // 사용자 정보 삭제
            userRepository.deleteByUserId(userId);

            log.info("사용자 삭제 완료: {}", userId);
        } catch (Exception e) {
            // DB 삭제 실패 등 기술적 예외 발생 시
            log.error("사용자 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new ApplicationException.UserDeleteFailedException(e);
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
        UserEntity userEntity = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    return new ApplicationException.UserNotFoundException(userId);
                });

        // 현재 비밀번호 검증 - UserSecurityService 사용
        userSecurityService.validateCurrentPassword(userEntity, currentPassword);

        try {
            // 새 비밀번호 암호화 및 저장 - UserSecurityService 사용
            String encodedNewPassword = userSecurityService.encodePassword(newPassword);
            userEntity.updateUserPassword(encodedNewPassword);
            userRepository.save(userEntity);

            log.info("비밀번호 변경 완료: {}", userId);
        } catch (Exception e) {
            log.error("비밀번호 변경 중 오류 발생: {}", e.getMessage(), e);
            throw new ApplicationException.PasswordUpdateFailedException(e);
        }
    }
}
