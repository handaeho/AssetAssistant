package kr.daeho.AssetAssistant.security;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import kr.daeho.AssetAssistant.auth.interfaces.AuthInterfaces;

/**
 * Spring Security 설정 클래스
 * 
 * 애플리케이션의 전반적인 보안 설정을 담당 -> HTTP 요청 권한, 인증 방식, 필터 등을 설정
 * 
 * @Component: 스프링 컨테이너에 빈을 등록하기 위한 어노테이션
 * @Configuration: 설정 정의 및 빈 정의 등 설정 관련 클래스를 위한 특수 목적의 어노테이션
 *                 - @Component의 특수화된 형태
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
    /**
     * 사용자 정보 서비스 주입
     */
    private final SecurityUserDetailService userDetailsService;

    /**
     * 인증 서비스 인터페이스 주입
     */
    private final AuthInterfaces authInterfaces;

    /**
     * AuthenticationManager 빈을 Spring 컨테이너에 등록
     * 
     * AuthService에서 필요로 하는 authenticationManager의 의존성 주입 수행
     * 
     * AuthenticationManager: 인증(Username/Password 등)을 처리하는 핵심 인터페이스
     * 
     * @param authenticationConfiguration: AuthenticationManager를 생성하거나 가져오는 설정 정보
     * @return AuthenticationManager 객체
     * @throws Exception 예외 발생 시 처리
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        // authenticationConfiguration 객체를 통해 실제 AuthenticationManager 인스턴스를 가져와서 반환
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 보안 필터 체인 설정
     * 
     * [설정 항목]
     * 1. CSRF 비활성화 (STATELESS 세션 정책)
     * 2. CORS 설정 적용
     * 3. URL 패턴별 접근 권한 설정
     * 4. 커스텀 필터 추가 (JWT 인증 및 인가)
     * 5. 세션 관리 정책 설정
     * 
     * 참고: Spring Security 6.1.0 부터 메서드 체이닝의 사용을 지양, 람다식을 통해 함수형으로 설정 지향
     * 
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception 예외 발생 시 처리
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("보안 필터 체인 설정");

        http
                // CSRF 보호 비활성화 (토큰 기반 인증의 경우 보통 필요 없기 때문)
                .csrf(csrf -> csrf.disable())
                // CORS 설정 적용 (corsConfigurationSource 빈 사용)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 세션 사용 X (무상태 세션 정책. JWT 기반 인증을 사용하므로 세션 관리 필요 X)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URL 별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로 설정
                        .requestMatchers("/",
                                "/api/auth/**",
                                "/api/users/signup",
                                "/api/users/check-id-duplicate",
                                "/h2-console/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated())
                // 폼 로그인 (기본 로그인 페이지) 비활성화 (REST API를 사용하므로)
                .formLogin(formLogin -> formLogin.disable())
                // HTTP 기본 인증 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())
                // 로그아웃 설정 (로그아웃 시, 홈 화면으로 리디렉션)
                .logout(logout -> logout.logoutSuccessUrl("/"))
                // JWT 인증 필터 추가 -> UsernamePasswordAuthenticationFilter 이전에 JWT 인증 필터를 실행하기 위함
                // addFilterBefore(): 지정한 필터를 특정 클래스에 해당하는 필터보다 앞에 추가
                // HTTP 요청 -> jwtAuthenticationFilter -> UsernamePasswordAuthenticationFilter
                .addFilterBefore(new JwtAuthenticationFilter(authInterfaces),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 인증 제공자 설정
     * 
     * 사용자 인증 시 필요한 설정(사용자 정보 조회 및 비밀번호 인코딩)을 적용한
     * DaoAuthenticationProvider 빈을 생성하는 역할
     * 
     * @return DaoAuthenticationProvider (사용자 인증을 처리하는 기본 제공 클래스)
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        log.debug("인증 제공자 설정");

        // DaoAuthenticationProvider 인스턴스 생성 및 인증에 필요한 설정 적용
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 사용자 정보를 조회할 때 사용할 UserDetailsService를 설정 (인증 과정에서 주체 결정)
        authProvider.setUserDetailsService(userDetailsService);
        // 비밀번호 검증에 사용할 PasswordEncoder를 설정
        authProvider.setPasswordEncoder(passwordEncoder());

        // 설정이 완료된 DaoAuthenticationProvider 객체 반환 및 Spring 컨테이너에 빈으로 등록
        // 이후, Spring Security의 인증 매니저에 의해 이 제공자가 사용되어 인증 과정을 처리
        return authProvider;
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
        log.debug("비밀번호 인코더 설정");

        // BCrypt 알고리즘을 사용하는 PasswordEncoder 빈을 생성 -> 모든 시큐리티 관련 클래스에서 주입받아 사용
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 설정 소스 빈 생성
     * 
     * CORS(Cross-Origin Resource Sharing):
     * 웹 애플리케이션에서 다른 출처의 리소스를 요청할 수 있도록 하는 보안 정책
     * 
     * Spring Security의 CORS 필터에서 사용할 설정을 제공 (Spring Security에서 권장하는 CORS 설정 방식)
     * 
     * SecurityFilterChain의 .cors() 메서드에서 이 빈을 사용
     * 
     * @return CorsConfigurationSource 객체
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration(); // CORS 설정 객체 생성

        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 허용할 출처
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(Arrays.asList("*")); // 허용할 헤더
        configuration.setAllowCredentials(true); // 인증 정보 허용

        // 인증 헤더도 클라이언트에 노출
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        // 1시간 동안 preflight 요청 캐싱
        configuration.setMaxAge(3600L);

        // UrlBasedCorsConfigurationSource: 특정 경로에 대한 CORS 설정을 정의하는 클래스
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 모든 경로에 대해 위의 설정을 적용
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
