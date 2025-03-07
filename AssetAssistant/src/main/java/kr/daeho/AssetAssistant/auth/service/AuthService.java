package kr.daeho.AssetAssistant.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import kr.daeho.AssetAssistant.auth.interfaces.AuthInterfaces;
import kr.daeho.AssetAssistant.auth.dto.TokenResponseDto;
import kr.daeho.AssetAssistant.auth.dto.LoginRequestDto;
import kr.daeho.AssetAssistant.security.JWTokenProvider;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;

/**
 * 사용자 로그인 서비스 -> 사용자 로그인 기능 담당
 * 
 * 컨트롤러에서 요청을 받아 비즈니스 로직을 처리하고, 결과를 반환
 * 핵심 비즈니스 로직 처리, 트랜잭션 관리, 예외 처리 등 웹 요청 및 응답을 위한 실제 로직 처리
 * 
 * 인터페이스를 상속받아 서비스를 구현(implements)함으로써,
 * 컨트롤러는 서비스(실제 구현체)가 아닌 인터페이스(계약)에 의존하여 의존성 역전 및 느슨한 결합 확보
 * 
 * NOTE: 비밀번호 비교 프로세스
 * 1. 사용자가 로그인 폼에 비밀번호 입력
 * 2. authenticationManager.authenticate()가 호출
 * 3. 내부적으로 DaoAuthenticationProvider가 SecurityUserDetailService를 통해 사용자 정보 로드
 * 4. PasswordEncoder(BCryptPasswordEncoder)를 사용해 입력된 비밀번호와 DB의 암호화된 비밀번호 비교
 * 5. BCrypt 알고리즘은 암호화된 문자열에서 솔트값을 추출하여 동일한 방식으로 입력된 비밀번호를 암호화한 후 해시값을 비교
 * 6. 비밀번호가 일치하면 인증이 성공하고, 일치하지 않으면 BadCredentialsException 예외가 발생
 * 
 * @Service: 서비스 클래스임을 명시
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션 (@Autowired 대신 사용)
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements AuthInterfaces {
    /**
     * 인증 관리자 (Spring Security의 인증을 처리)
     */
    private final AuthenticationManager authenticationManager;

    /**
     * JWT 토큰 제공자 (토큰 생성, 검증 등을 담당)
     */
    private final JWTokenProvider tokenProvider;

    /**
     * 로그인 처리 메소드
     * 
     * UsernamePasswordAuthenticationToken: 사용자 인증정보를 캡슐화하는 인터페이스
     * -> Spring Security의 Authentication 인터페이스 구현체
     * 
     * @param loginRequest 로그인 요청 DTO
     * @return 토큰 응답 DTO
     * @throws ApplicationException.AuthenticationFailedException 인증 실패 시 발생
     */
    @Override
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("로그인 시도: {}", loginRequestDto.getUserId());

        try {
            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequestDto.getUserId(),
                    loginRequestDto.getPassword());

            // 인증 관리자에게 인증 객체 전달 (authenticationManager, 인증 프로세스 시작)
            Authentication authentication = authenticationManager.authenticate(authToken);

            // 인증 정보 저장 (SecurityContextHolder: 애플리케이션 전체에서 현재 사용자의 인증 정보를 참조 가능)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 액세스 토큰 및 리프레시 토큰 생성
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            log.info("로그인 성공: {}", loginRequestDto.getUserId());

            // 토큰 응답 DTO 객체 생성 및 반환
            return TokenResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(tokenProvider.getTokenExpirationTime())
                    .tokenType("Bearer")
                    .build();
        } catch (Exception e) {
            log.error("로그인 실패: {}", loginRequestDto.getUserId(), e);
            throw new ApplicationException.LoginFailedException(loginRequestDto.getUserId());
        }
    }
}
