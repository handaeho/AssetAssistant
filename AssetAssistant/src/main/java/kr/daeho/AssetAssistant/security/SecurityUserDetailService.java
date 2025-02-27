package kr.daeho.AssetAssistant.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import kr.daeho.AssetAssistant.auth.entity.AuthEntity;
import kr.daeho.AssetAssistant.auth.repository.AuthRepository;

/**
 * SecurityUserDetailsService 클래스는 스프링 시큐리티의 UserDetailsService(인터페이스)를 구현함
 * 
 * 인증 시 DB에서 회원 정보를 조회한 후, 스프링 시큐리티가 요구하는 UserDetails 객체로 변환
 * 
 * [인증 과정]의 6 ~ 9번 단계
 * 
 * 6. 사용자 아이디를 UserDetailsService에게 전달
 * -> AuthenticationProvider가 전달받은 토큰에서 사용자 아이디를 추출, 전달
 * 
 * 7. UserDetailsService에서 사용자 아이디를 기반으로 UserDetails 객체 생성
 * -> UserDetailsService의 loadUserByUsername() 메서드: 사용자 아이디를 받아 사용자 상세 정보 조회 수행
 * 
 * 8. 사용자 아이디를 기반으로 DB에서 사용자 상세 정보 조회
 * -> DB 뿐만 아니라 외부 시스템이나 캐시를 조회할 수도 있음
 * 
 * 9. 조회된 정보를 담은 UserDetails 객체를 AuthenticationProvider에게 반환
 * -> 이름, 암호화 된 비밀번호, 권한, 활성화 상태 등
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
    // 사용자 인증 정보 조회를 위한 리포지토리 주입
    private final AuthRepository authRepository;

    /**
     * 사용자 정보 조회
     * 
     * User: UserDetails 인터페이스의 기본 구현체
     * - 사용자 이름(아이디), 비밀번호, 권한(roles/authorities) 정보를 가짐
     * 
     * [인증 과정 7단계]: UserDetailsService의 loadUserByUsername() 메서드 호출
     * 
     * [인증 과정 8단계]: 사용자 아이디를 기반으로 DB에서 사용자 상세 정보 조회
     * 
     * [인증 과정 9단계]: 조회된 정보를 담은 UserDetails 객체 반환
     * 
     * @param userId 사용자 아이디
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 사용자 정보를 찾을 수 없는 경우 발생
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.info("사용자 정보 로드 시작: {}", userId);

        // 사용자 아이디를 기반으로 DB에서 사용자 상세 정보 조회
        AuthEntity authEntity = authRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다: " + userId));

        log.debug("사용자 정보 로드 완료: {}", userId);

        // 조회된 사용자 정보를 기반으로 UserDetails 객체 생성 및 반환
        return new User(
                authEntity.getUserId(), // 사용자 이름(아이디)
                authEntity.getUserPassword(), // 사용자 비밀번호
                // Collections.singletonList(): 단 하나의 요소만 가지는 불변 리스트 (리스트 사이즈가 1로 고정, 변경 불가)
                // SimpleGrantedAuthority(): 사용자 권한 정보를 가지는 객체
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))); // 사용자 권한
    }

}
