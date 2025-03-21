package kr.daeho.AssetAssistant.auth.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 토큰 블랙리스트 서비스
 * 
 * 로그아웃 처리된 토큰이나 보안상 문제가 있는 토큰을 블랙리스트로 관리하는 서비스
 * Redis를 사용하여 블랙리스트 토큰을 저장하고 검증
 * 
 * @Service: 서비스 클래스임을 명시
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션 (@Autowired 대신 사용)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    /**
     * Redis 템플릿 (토큰 블랙리스트 저장)
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 블랙리스트 키 프리픽스
     */
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    /**
     * 토큰을 블랙리스트에 추가
     * 
     * @param token 블랙리스트에 추가할 토큰
     * @param ttl   블랙리스트 유지 시간 (초)
     */
    public void addToBlacklist(String token, long ttl) {
        // 블랙리스트 키 생성 (프리픽스 + 토큰)
        String blacklistKey = BLACKLIST_PREFIX + token;

        // 블랙리스트 키 저장
        redisTemplate.opsForValue().set(blacklistKey, "true");

        // 블랙리스트 키 만료 시간 설정
        redisTemplate.expire(blacklistKey, ttl, TimeUnit.SECONDS);

        log.debug("토큰이 블랙리스트에 추가됨: {}, 만료 시간: {}초", token, ttl);
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * 
     * @param token 검증할 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    public boolean isBlacklisted(String token) {
        // 블랙리스트 키 생성 (프리픽스 + 토큰)
        String blacklistKey = BLACKLIST_PREFIX + token;

        // 블랙리스트 키 존재 여부 확인
        Boolean exists = redisTemplate.hasKey(blacklistKey);

        // 존재 여부 반환
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 여러 토큰을 블랙리스트에 추가 (여러 기기에서 동시 로그아웃)
     * 
     * @param tokens 블랙리스트에 추가할 토큰 배열
     * @param ttl    블랙리스트 유지 시간 (초)
     */
    public void addAllToBlacklist(String[] tokens, long ttl) {
        // 여러개의 키를 블랙리스트에 추가
        for (String token : tokens) {
            addToBlacklist(token, ttl);
        }

        log.debug("{}개의 토큰이 블랙리스트에 추가됨", tokens.length);
    }
}