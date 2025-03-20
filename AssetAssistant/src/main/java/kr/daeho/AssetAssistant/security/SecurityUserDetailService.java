package kr.daeho.AssetAssistant.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;

import kr.daeho.AssetAssistant.common.exception.ApplicationException;
import kr.daeho.AssetAssistant.users.entity.UserEntity;
import kr.daeho.AssetAssistant.users.repository.UserRepository;

/**
 * Spring Security의 UserDetailsService 인터페이스 구현 서비스 클래스
 * 
 * Spring Security가 사용자 인증 수행 시, 사용자 정보를 DB에서 조회하여 제공하는 역할
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
public class SecurityUserDetailService implements UserDetailsService {
    /**
     * 사용자 정보를 조회하기 위한 리포지토리 주입
     */
    private final UserRepository userRepository;

    /**
     * userId로 DB에서 사용자 정보 조회
     * 
     * @param userId 사용자 아이디
     * @return UserDetails 사용자 상세 정보 (Spring Security의 사용자 정보가 담긴 인터페이스)
     * @throws ApplicationException.UserNotFoundException 사용자를 찾을 수 없을 때 발생
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws ApplicationException.UserNotFoundException {
        log.debug("사용자 정보 조회: {}", userId);

        // DB에서 사용자 정보 조회 후, UserEntity 객체 생성
        UserEntity userEntity = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다: {}", userId);
                    return new ApplicationException.UserNotFoundException(userId);
                });

        // Spring Security의 User 객체 생성 및 반환 (사용자 인증 객체 생성)
        // User 객체는 UserDetails 인터페이스를 구현하고 있어, 인증 및 권한 검증에 사용 가능
        return User.builder()
                .username(userEntity.getUserId()) // DB에 저장된 사용자 아이디 (인증 주체)
                .password(userEntity.getUserPassword()) // DB에 저장된 암호화된 비밀번호
                .roles(userEntity.getRole().name()) // DB에 저장된 사용자 역할 (권한)
                .build();
    }
}
