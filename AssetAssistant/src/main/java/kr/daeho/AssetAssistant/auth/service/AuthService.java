package kr.daeho.AssetAssistant.auth.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import kr.daeho.AssetAssistant.auth.entity.TokenRedisEntity;
import kr.daeho.AssetAssistant.auth.repository.TokenRedisRepository;
import kr.daeho.AssetAssistant.auth.interfaces.AuthInterfaces;
import kr.daeho.AssetAssistant.auth.dto.LoginRequestDto;
import kr.daeho.AssetAssistant.security.JWTokenProvider;
import kr.daeho.AssetAssistant.common.exception.ApplicationException;

/**
 * 사용자 인증 서비스 -> 사용자 로그인, 토큰 관리 등 인증 관련 기능 담당
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
     * Redis 리포지토리 (세션 및 토큰 저장)
     */
    private final TokenRedisRepository tokenRedisRepository;

    /**
     * 로그인 처리 메소드 - 사용자 인증 후 토큰 생성
     * 
     * @param loginRequestDto 로그인 요청 DTO
     * @return Map<String, Object> 로그인 결과 정보 (사용자 ID, 액세스 토큰, 리프레시 토큰)
     * @throws ApplicationException.LoginFailedException 인증 실패 시 발생
     */
    @Override
    public Map<String, Object> login(LoginRequestDto loginRequestDto) {
        log.info("로그인 시도: {}", loginRequestDto.getUserId());

        try {
            // 인증 객체 생성 (사용자 아이디, 비밀번호)
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequestDto.getUserId(),
                    loginRequestDto.getPassword());

            // 인증 관리자에게 인증 객체 전달 (authenticationManager: 인증 프로세스 시작)
            Authentication authentication = authenticationManager.authenticate(authToken);

            // 인증 정보 저장 (SecurityContextHolder: 애플리케이션 전체에서 현재 사용자의 인증 정보를 참조 가능)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 사용자 ID 추출
            String userId = authentication.getName();

            // 디바이스 ID 가져오기 (요청에서 가져오거나 기본값 사용)
            String deviceId = loginRequestDto.getDeviceId() != null ? loginRequestDto.getDeviceId() : "default";

            // 토큰 생성 및 저장
            String accessToken = tokenProvider.generateAccessToken(userId, deviceId);
            String refreshToken = tokenProvider.generateRefreshToken(userId, deviceId);

            // Redis에 토큰 정보 저장 (사용자 ID와 디바이스 ID를 복합키로 사용)
            String tokenId = TokenRedisEntity.createId(userId, deviceId);

            TokenRedisEntity tokenRedis = TokenRedisEntity.builder()
                    .id(tokenId)
                    .userId(userId)
                    .deviceId(deviceId)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .createdAt(LocalDateTime.now())
                    .build();

            tokenRedisRepository.save(tokenRedis);

            // 응답 데이터 생성
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("userId", userId);
            tokenInfo.put("deviceId", deviceId);
            tokenInfo.put("accessToken", accessToken);
            tokenInfo.put("refreshToken", refreshToken);

            log.info("로그인 성공. 토큰 생성 완료: userId={}, deviceId={}", userId, deviceId);

            return tokenInfo;
        } catch (Exception e) {
            log.error("로그인 실패: {}", loginRequestDto.getUserId(), e);
            throw new ApplicationException.LoginFailedException(loginRequestDto.getUserId());
        }
    }

    /**
     * 토큰 검증 메소드
     * 
     * @param accessToken 액세스 토큰
     * @return 사용자 ID
     */
    @Override
    public String validateToken(String accessToken) {
        log.debug("토큰 검증 요청");

        // 토큰 유효성 검증
        if (!tokenProvider.validateToken(accessToken)) {
            throw new ApplicationException.InvalidTokenException("유효하지 않은 토큰입니다");
        }

        // 토큰에서 사용자 ID 추출
        String userId = tokenProvider.getUserIdFromToken(accessToken);

        // Redis에서 토큰 확인
        tokenRedisRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new ApplicationException.InvalidTokenException("저장된 토큰 정보가 없습니다"));

        log.debug("토큰 검증 성공: {}", userId);

        return userId;
    }

    /**
     * 토큰 갱신 메소드 - 리프레시 토큰으로 새 액세스 토큰 발급
     * 
     * @param refreshToken 리프레시 토큰
     * @return Map<String, Object> 새 토큰 정보 갱신 정보 (사용자 ID, 액세스 토큰, 리프레시 토큰)
     */
    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        log.info("토큰 갱신 요청");

        // 리프레시 토큰 유효성 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new ApplicationException.InvalidTokenException("유효하지 않은 리프레시 토큰입니다");
        }

        // 토큰에서 사용자 ID 및 디바이스 ID 추출
        String userId = tokenProvider.getUserIdFromToken(refreshToken);
        String deviceId = tokenProvider.getDeviceIdFromToken(refreshToken);

        // Redis에서 리프레시 토큰으로 저장된 토큰 정보 조회
        // Iterable을 List로 변환하여 stream() 사용 (StreamSupport.stream(): 데이터 처리 효율성 향상)
        List<TokenRedisEntity> tokensList = StreamSupport.stream(
                tokenRedisRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

        // tokensList에서 리프레시 토큰 정보 조회
        Optional<TokenRedisEntity> existingToken = tokensList.stream()
                .filter(token -> refreshToken.equals(token.getRefreshToken()))
                .findFirst();

        if (existingToken.isEmpty()) {
            throw new ApplicationException.InvalidTokenException("저장된 리프레시 토큰 정보가 없습니다");
        }

        // 새 액세스 토큰 생성
        String newAccessToken = tokenProvider.generateAccessToken(userId, deviceId);

        // Redis에 토큰 정보 업데이트
        TokenRedisEntity tokenRedis = existingToken.get();
        tokenRedis.setAccessToken(newAccessToken);
        tokenRedisRepository.save(tokenRedis);

        // 응답 데이터 생성
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("userId", userId);
        tokenInfo.put("deviceId", deviceId);
        tokenInfo.put("accessToken", newAccessToken);
        tokenInfo.put("refreshToken", refreshToken);

        log.info("토큰 갱신 성공: userId={}, deviceId={}", userId, deviceId);
        return tokenInfo;
    }

    /**
     * 사용자의 특정 디바이스에서 로그아웃 처리 메소드
     * 
     * @param userId   사용자 ID
     * @param deviceId 디바이스 ID
     */
    public void logout(String userId, String deviceId) {
        log.info("로그아웃 요청: userId={}, deviceId={}", userId, deviceId);

        // 복합키 생성
        String tokenId = TokenRedisEntity.createId(userId, deviceId);

        // Redis에서 토큰 정보 조회
        tokenRedisRepository.findById(tokenId).ifPresent(tokenRedis -> {
            // 토큰 블랙리스트에 추가
            String accessToken = tokenRedis.getAccessToken();
            String refreshToken = tokenRedis.getRefreshToken();
            tokenProvider.blacklistToken(accessToken);
            tokenProvider.blacklistToken(refreshToken);

            // Redis에서 토큰 정보 삭제
            tokenRedisRepository.deleteById(tokenId);
            log.info("토큰이 블랙리스트에 추가되고 Redis에서 삭제됨");
        });

        // SecurityContext에서 인증 정보 제거
        SecurityContextHolder.clearContext();

        log.info("로그아웃 성공: userId={}, deviceId={}", userId, deviceId);
    }

    /**
     * 사용자의 모든 디바이스에서 로그아웃 처리 메소드
     * 
     * @param userId 사용자 ID
     */
    @Override
    public void logout(String userId) {
        log.info("모든 디바이스에서 로그아웃 요청: {}", userId);

        // 사용자 ID로 모든 토큰 조회
        List<TokenRedisEntity> userTokens = tokenRedisRepository.findAllByUserId(userId);

        for (TokenRedisEntity token : userTokens) {
            // 각 토큰을 블랙리스트에 추가
            tokenProvider.blacklistToken(token.getAccessToken());
            tokenProvider.blacklistToken(token.getRefreshToken());
        }

        // Redis에서 사용자의 모든 토큰 정보 삭제
        tokenRedisRepository.deleteAllByUserId(userId);

        // SecurityContext에서 인증 정보 제거
        SecurityContextHolder.clearContext();

        log.info("모든 디바이스에서 로그아웃 성공: {}", userId);
    }
}
