package kr.daeho.AssetAssistant.auth.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 * Redis에 저장되는 토큰 정보를 관리하는 엔티티
 * 
 * B 테이블과 1:1 또는 1:N 관계로 매핑되는 객체로 DB에 저장되거나 조회되는 데이터의 단위
 * 
 * Entity는 DTO와 달리, 비즈니스 로직이나 연관관계 관리 메소드 등을 가질 수 있음
 * 
 * 여러 기기에서의 로그인을 지원하기 위해 userId와 deviceId를 조합하여 복합 키를 생성
 * 
 * @Getter: 모든 필드의 Getter 메서드를 자동으로 생성. setter는 의도적으로 제외하여 불변성 유지
 * @AllArgsConstructor: 모든 필드를 매개변수로 하는 생성자를 자동으로 생성
 * @Builder: 빌더 패턴을 사용하여 객체 생성을 용이하게 함
 * @RedisHash: Redis에 저장되는 토큰 정보를 관리하는 엔티티
 *             - value: Redis에 저장되는 토큰 정보의 키 값
 *             - timeToLive: 토큰 정보의 유효 기간
 *             (60 * 60 * 24 * 7 = 60초 * 60분 * 24시간 * 7일)
 */
@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "token", timeToLive = 60 * 60 * 24 * 7) // 리프레시토큰과 expiretime 일치
public class TokenRedisEntity {
    /**
     * 토큰 정보의 고유 복합 식별자 (userId:deviceId 형식)
     * 
     * @Id: 해당 필드가 테이블의 PK임을 명시
     */
    @Id
    private String id; // 복합 키: userId:deviceId

    /**
     * 사용자 ID
     * 
     * @Indexed: 보조 인덱스 설정
     */
    @Indexed
    private String userId;

    /**
     * 디바이스 ID (기기 고유 식별자)
     */
    private String deviceId;

    /**
     * 액세스 토큰 정보
     * 
     * @Indexed: 보조 인덱스 설정
     */
    @Indexed
    private String accessToken;

    /**
     * 리프레시 토큰 정보
     */
    private String refreshToken;

    /**
     * 토큰 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 복합 키 생성을 위한 정적 메서드
     * 
     * @param userId   사용자 ID
     * @param deviceId 디바이스 ID
     * @return 복합 키 (userId:deviceId)
     */
    public static String createId(String userId, String deviceId) {
        return userId + ":" + deviceId;
    }

    /**
     * 액세스 토큰 업데이트 메서드
     * 
     * @param accessToken 새로운 액세스 토큰
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
