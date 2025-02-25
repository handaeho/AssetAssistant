package kr.daeho.AssetAssistant.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 스프링 시큐리티의 전반적인 설정 담당 클래스
 * 
 * [사용자 인증 과정]
 * 
 * 1. 사용자 아이디 패스워드 입력
 * -> 클라이언트가 로그인 폼에 아이디와 비밀번호를 입력
 * 
 * 2. HTTPServletRequest 객체를 통해 입력된 아이디, 비밀번호 정보를 서버로 전달
 * -> HTTPServletRequest: 서블릿 컨테이너(웹 요청 처리 기본 단위, Tomcat 등)가 생성하는 객체
 * -> 입력 정보가 HTTP 요청의 본문이나 파라미터로 서버에 전달
 * 
 * 3. AuthenticationFilter에서 유효성 검사 & UsernamePasswordAuthenticationToken 객체 생성
 * -> UsernamePasswordAuthenticationToken: Authentication 인터페이스를 구현, 사용자 인증정보 저장
 * -> 사용자의 아이디와 비밀번호를 추출하여 UsernamePasswordAuthenticationToken 객체를 생성
 * -> JWT 기반 인증 적용 시, 커스텀 필터나 커스텀 성공 핸들러를 사용해 토큰을 생성
 * 
 * 4. AuthenticationManager에 UsernamePasswordAuthenticationToken 전달. 인증 처리 시작
 * -> AuthenticationManager: 인증 처리 과정을 담당하는 핵심 컴포넌트
 * -> 여러 AuthenticationProvider를 순차적으로 호출할 수도 있음
 * 
 * 5. AuthenticationProvider에게 UsernamePasswordAuthenticationToken 전달
 * -> AuthenticationProvider: 실제 인증 로직 처리
 * -> 기본적으로는 DaoAuthenticationProvider 사용 (AuthenticationManager 내에 존재)
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
 * 10. 입력한 정보와 UserDetails 객체의 정보 비교, 실제 인증 처리
 * -> AuthenticationProvider에서 입력 비밀번호와 UserDetails에서 반환한 (DB에 저장된) 암호화 비밀번호 비교
 * 
 * 11. 인증 성공 시, SecurityContextHolder에 Authentication을 저장
 * -> SecurityContextHolder: 애플리케이션 전체에서 현재 사용자의 인증 정보를 참조 가능
 * -> 이 후, 요청 처리 과정에서 @AuthenticationPrincipal 등을 사용해 사용자 정보를 참조
 * 
 * 12. 성공 시, AuthenticationSuccessHandler 핸들러 실행
 * -> 사용자를 홈 화면이나 요청했던 페이지로 리디렉션 등
 * 
 * 13. AuthenticationSuccessHandler에서 JWT 발급 수행
 * -> 인증 성공 후, 커스텀 AuthenticationSuccessHandler (또는 필터 내 로직)에서
 * -> 사용자 정보(예: 아이디, 권한 등)를 기반으로 JWT를 생성
 * -> 생성된 JWT 토큰을 HTTP 응답 헤더(예: Authorization)나 응답 본문에 포함하여 클라이언트에게 전달
 * 
 * 14. 클라이언트는 JWT를 저장 후 후속 요청 시 사용
 * -> 클라이언트는 JWT를 로컬 저장소(예: localStorage) 등에 저장하고,
 * -> 이후 요청 시 HTTP 헤더에 포함하여 전송
 * 
 * 15. 후속 요청에 대한 JWT 인증 처리
 * -> 각 요청이 들어올 때, JWTAuthorizationFilter 같은 커스텀 인증 필터가 JWT를 추출하여 검증
 * -> 토큰이 유효하면, 해당 사용자 정보를 기반으로 SecurityContextHolder에 인증 정보를 설정하여 요청을 처리
 * 
 * 16. 실패 시, AuthenticationFailureHandler 핸들러 실행
 * -> 실패 메시지를 포함하여 로그인 페이지로 리디렉션 등
 * 
 * [요약]
 * 
 * > 인증 필터: UsernamePasswordAuthenticationFilter
 * -> 로그인 요청 처리, 사용자 자격 증명 추출, 토큰 생성
 * 
 * > 인증 매니저: AuthenticationManager
 * -> 인증 프로세스 조율, 실제 인증 로직을 수행해 사용자 정보 검증의 주체
 * 
 * > 사용자 정보 로드: UserDetailsService
 * -> 사용자 아이디를 받아 DB에서 사용자 상세 정보 조회
 * 
 * > 비밀번호 비교: PasswordEncoder
 * -> 입력 비밀번호와 DB에 저장된 암호화 비밀번호 비교
 * 
 * > 인증정보 저장: SecurityContextHolder
 * -> 인증 정보 저장, 인증 사용자 정보 유지, 애플리케이션 전반에 공유
 * 
 * > 성공/실패 핸들러: AuthenticationSuccessHandler, AuthenticationFailureHandler
 * -> 인증 성공/실패 시 처리 로직 정의, JWT 발급 등
 * 
 * @Configuration: @Component의 특수화된 형태
 *                 - @Component: 스프링 컨테이너에 빈을 등록하기 위한 어노테이션
 *                 - @Configuration: 설정 정의 및 빈 정의 등 설정 관련 클래스를 위한 특수 목적의 어노테이션
 *                 - 내부에서 싱글톤 보장을 위한 CGLIB 프록시를 사용하기 위함
 *                 -> 내부의 @Bean 메서드 호출을 가로채어 싱글톤 보장을 위해 CGLIB 프록시를 적용해서,
 *                 -> @Configuration를 적용한 클래스 내에서 @Bean이 적용된 메소드를 통해 빈을 생성하고,
 *                 -> 이 메소드를 여러 번 요청해도 같은 인스턴스가 반환되는 것을 보장
 * @EnableWebSecurity: 스프링 시큐리티 활성화 및 웹 보안 설정 구성
 *                     - 스프링 시큐리티의 설정을 정의한 구성 클래스에서 선언
 *                     - 자동으로 스프링 시큐리티 필터 체인 생성 및 웹 모안 활성화
 *                     - 스프링 시큐리티 필터 체인: 스프링 시큐리티에서 제공하는 인증 및 인가를 위한 필터 모음
 *                     - HTTP 요청 -> 웹 어플리케이션 서버 -> 필터 1 -> ... -> 필터 N -> 컨트롤러
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션 (@Autowired 대신 사용)
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    // JWT 인증 필터 주입
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 사용자 보안 관련 상세 정보 로드 서비스 주입
    // final로 선언해 불변성 보장, @RequiredArgsConstructor로 생성자 자동 생성 및 의존성 주입
    private final SecurityUserDetailService userDetailsService;

    /**
     * 보안 필터 체인 설정
     * 
     * [설정 항목]
     * 
     * 1. CSRF 비활성화(STATELESS 세션 정책)
     * 
     * 2. URL 패턴별 접근 권한 설정
     * 
     * 3. 커스텀 필터 추가(JWT 인증/인가)
     * 
     * 4. 세션 관리 정책 설정
     * 
     * 참고: Spring Security 6.1.0 부터 메서드 체이닝의 사용을 지양, 람다식을 통해 함수형으로 설정 지향
     * 
     * @param http: HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception 예외 발생 시 처리
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화 (토큰 기반 인증의 경우 보통 필요 없기 때문)
                .csrf((csrfConfig) -> csrfConfig.disable())
                // 세션 사용 X (무상태 세션 정책. JWT 기반 인증 사용 위함, 세션 관리 필요 X)
                .sessionManagement(
                        (sessionConfig) -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 인증에 대한 요청 설정
                .authorizeHttpRequests((authz) -> authz
                        // 인증이 필요 없는 경로 설정 (회원가입, 로그인 등)
                        .requestMatchers("/", "/api/auth/**", "/login/**").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated())
                // 폼 로그인 등 기본 로그인 페이지 비활성화
                .formLogin((formLoginConfig) -> formLoginConfig.disable())
                // 로그아웃 설정 (로그아웃 시, 홈 화면으로 리디렉션)
                .logout((logoutConfig) -> logoutConfig.logoutSuccessUrl("/"));

        // [인증 과정 15단계]: JWT 필터 추가
        // UsernamePasswordAuthenticationFilter 이전에 JWT 인증 필터 실행
        // addFilterBefore(): 지정한 필터를 특정 클래스에 해당하는 필터보다 앞에 추가
        // HTTP 요청 -> jwtAuthenticationFilter -> UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 스프링 시큐리티의 AuthenticationProvider 인증 공급자 설정 (DAO 기반, 사용자 인증 처리)
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // 사용자 상세 정보를 로드하는 서비스
        authProvider.setPasswordEncoder(passwordEncoder()); // 비밀번호 암호화 알고리즘
        return authProvider;
    }

    /**
     * AuthenticationManager 빈 등록 (인증 요청 처리)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCryptPasswordEncoder를 빈으로 등록하고, 애플리케이션 내 필요한 곳에서 이 빈을 주입받아 사용
     * 
     * BCrypt 알고리즘: 비밀번호 암호화 알고리즘
     * - 해싱 속도 조절을 위한 비용인자 사용
     * - 솔트 값 내장 -> 동일한 비밀번호에 대해 다른 해시 생성
     * - 해시 결과로 60자 길이의 문자열 출력 -> $[알고리즘 버전]$[비용 인자]$[Salt+해시 결과] (솔트 22자, 해시 31자)
     * 
     * 비용인자 설정 -> 고유 솔트값 생성 -> 원문 비밀번호 + 솔트 -> 해시 연산 -> 60자 길이의 해시 결과 생성
     * 
     * 비밀번호 입력 -> DB에 저장된 해시 문자열에서 비용인자와 솔트 추출 -> 원문 비밀번호에 똑같이 해시 연산 적용 -> 해시 결과 비교
     * 
     * 비용 인자(Cost Factor): 암호 해시 알고리즘에서 해싱 처리에 필요한 연산 횟수
     * - 비용 인자가 n이라면, 해싱 연산은 2^n
     * - 높을수록 해시 생성 시간 증가 -> 부르트포스 방지
     * 
     * 해시(Hash): 임의의 길이를 가진 입력값(데이터)을 고정된 길이의 출력값으로 변환
     * - 고정된 길이 출력 -> 입력 데이터의 크기와 무관하게 항상 동일한 길이의 해시 값 생성
     * - 일방향성 -> 해시 값을 통해 원래 입력값을 역으로 계산하는 것은 불가능
     * - 충돌 회피 -> 다른 두 입력값이 같은 해시 값을 생성 불가
     * - 민감한 변경성 -> 입력값이 조금이라도 달라지면 해시 값은 완전히 달라짐
     * 
     * 데이터베이스에는 원본 비밀번호 대신 해시 값만 저장
     * - 비밀번호가 같아도, 솔트 값이 다르므로 전체 해시 값은 다르게 생성
     * 
     * @return BCryptPasswordEncoder 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 알고리즘을 사용하는 PasswordEncoder 빈을 생성
        // 모든 시큐리티 관련 클래스에서 주입받아 사용
        return new BCryptPasswordEncoder();
    }
}
