package kr.daeho.AssetAssistant.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.daeho.AssetAssistant.users.repository.UserReposiory;
import kr.daeho.AssetAssistant.users.interfaces.UserSignupInterfaces;
import kr.daeho.AssetAssistant.users.dto.UserDto;
import kr.daeho.AssetAssistant.users.entity.UserEntity;
import kr.daeho.AssetAssistant.users.enums.UserRoleEnum;
import kr.daeho.AssetAssistant.users.dto.SignupRequestDto;
import kr.daeho.AssetAssistant.common.constant.ErrorCode;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;
import kr.daeho.AssetAssistant.common.utils.ModelMapper;

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
    private final UserReposiory userRepository; // 사용자 정보 저장을 위한 리포지토리
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화를 위한 패스워드 인코더
    private final ModelMapper modelMapper; // 엔티티와 DTO 간 변환을 위한 모델 매퍼

    /**
     * 사용자 회원가입
     * 
     * @param signupRequestDto 사용자 회원가입 요청 DTO
     * @return UserDto
     * @throws ApplicationExceptions.UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public UserDto signup(SignupRequestDto signupRequestDto) {
        log.info("사용자 회원가입 요청 처리: {}", signupRequestDto.getUserId());

        // 사용자 아이디 중복 체크
        if (userRepository.existsByUserId(signupRequestDto.getUserId())) {
            log.error("사용자 아이디 중복: {}", signupRequestDto.getUserId());
            throw new ApplicationException.UserAlreadyExistsException(signupRequestDto.getUserId());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        // 사용자 생성
        UserEntity userEntity = UserEntity.builder()
                .userId(signupRequestDto.getUserId())
                .userPassword(encodedPassword)
                .userName(signupRequestDto.getUserName())
                .role(UserRoleEnum.ROLE_USER)
                .build();

        // 사용자 저장
        userRepository.save(userEntity);

        return modelMapper.toUserDto(userEntity);

    }
}
