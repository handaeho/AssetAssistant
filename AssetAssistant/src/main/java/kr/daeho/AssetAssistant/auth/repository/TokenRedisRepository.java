package kr.daeho.AssetAssistant.auth.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

import kr.daeho.AssetAssistant.auth.entity.TokenRedisEntity;

/**
 * Redis에 저장되는 토큰 정보를 관리하는 레포지토리
 * 
 * CrudRepository: 기본적인 CRUD 작업을 지원하는 인터페이스
 */
public interface TokenRedisRepository extends CrudRepository<TokenRedisEntity, String> {

    /**
     * AccessToken으로 토큰 찾아내기
     * 
     * Optional: 값이 없을 수도 있는 경우에 사용 (NullPointerException 방지)
     * 
     * @param accessToken 액세스 토큰
     * @return Optional<TokenRedis> 토큰 정보
     */
    Optional<TokenRedisEntity> findByAccessToken(String accessToken);

    /**
     * 사용자 ID로 모든 토큰 찾기 (여러 기기에서 로그인한 경우를 위함)
     * 
     * @param userId 사용자 ID
     * @return List<TokenRedis> 사용자의 모든 토큰 정보
     */
    List<TokenRedisEntity> findAllByUserId(String userId);

    /**
     * 사용자 ID로 모든 토큰 삭제하기 (모든 기기에서 로그아웃)
     * 
     * @param userId 사용자 ID
     */
    void deleteAllByUserId(String userId);
}
