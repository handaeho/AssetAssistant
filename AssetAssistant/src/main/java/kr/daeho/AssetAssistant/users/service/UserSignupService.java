package kr.daeho.AssetAssistant.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import kr.daeho.AssetAssistant.users.repository.UserRepository;
import kr.daeho.AssetAssistant.users.interfaces.UserSignupInterfaces;
import kr.daeho.AssetAssistant.users.dto.UserDto;
import kr.daeho.AssetAssistant.users.entity.UserEntity;
import kr.daeho.AssetAssistant.users.dto.SignupRequestDto;
import kr.daeho.AssetAssistant.common.utils.ModelMapper;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;

/**
 * 사용자 회원가입 서비스 -> 사용자 회원가입 기능 담당
 * 
 * 컨트롤러에서 요청을 받아 비즈니스 로직을 처리하고, 결과를 반환
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
public class UserSignupService implements UserSignupInterfaces {
    // final로 선언해 불변성 보장, @RequiredArgsConstructor로 생성자 자동 생성 및 의존성 주입
    private final UserRepository userRepository; // 사용자 정보 저장을 위한 리포지토리
    private final UserSecurityService securityService; // 보안 관련 로직을 위한 서비스
    private final ModelMapper modelMapper; // 엔티티와 DTO 간 변환을 위한 모델 매퍼

    /**
     * 아이디 중복 체크
     * 
     * @param userId 아이디
     * @return 중복 여부
     */
    @Override
    public boolean isUserIdDuplicate(String userId) {
        return userRepository.existsByUserId(userId);
    }

    /**
     * 사용자 회원가입
     * 
     * @param signupRequestDto 사용자 회원가입 요청 DTO
     * @return UserDto 사용자 정보 DTO
     */
    @Override
    @Transactional
    public UserDto signup(SignupRequestDto signupRequestDto) {
        log.info("사용자 회원가입 요청 처리: {}", signupRequestDto.getUserId());

        // 아이디 중복 최종 확인 (동시성 문제 해결)
        checkUserIdDuplicate(signupRequestDto.getUserId());

        // 비밀번호 암호화 - 보안 서비스로 위임
        String encodedPassword = securityService.encodePassword(signupRequestDto.getPassword());

        // ModelMapper를 사용하여 DTO를 Entity로 변환
        UserEntity userEntity = modelMapper.signUpRequestToUserEntity(signupRequestDto, encodedPassword);

        // 사용자 저장
        userRepository.save(userEntity);

        log.info("회원가입 완료: {}", signupRequestDto.getUserId());

        return modelMapper.toUserDto(userEntity);
    }

    /**
     * 아이디 중복 확인 (동시성 고려)
     * 
     * 레이스 컨디션 방지 (같은 자원에 여러 프로세스가 동시에 접근해 데이터 불일치 방지)
     * 
     * synchronized: 현재 데이터에 접근 중인 스레드를 제외하고, 다른 스레드는 접근 방지
     * 
     * 같은 아이디를 서로 다른 사용자가 동시에 등록하려고 할 때, 데이터 불일치 방지
     * 
     * @param userId 사용자 아이디
     * @throws ApplicationException.UserAlreadyExistsException 아이디 중복 시
     */
    private void checkUserIdDuplicate(String userId) {
        synchronized (this.getClass()) {
            if (userRepository.existsByUserId(userId)) {
                throw new ApplicationException.UserAlreadyExistsException(userId);
            }
        }
    }
}
