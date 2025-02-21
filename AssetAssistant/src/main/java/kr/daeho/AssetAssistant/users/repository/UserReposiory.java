package kr.daeho.AssetAssistant.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import kr.daeho.AssetAssistant.users.entity.UserEntity;

/**
 * 사용자 레파지토리
 * 
 * 클라이언트 <-> 컨트롤러 <-> 인터페이스 <-> 서비스 <-> 레파지토리 <-> DB
 * 
 * 레파지토리의 주요 역할: DB와의 직접적인 상호작용. 데이터 영속성 유지.
 * 
 * 데이터 접근 로직을 한 곳에서 관리하여 코드 중복 방지
 * DB 연산을 추상화하여 비즈니스 로직과 분리
 * 테스트 용이성 향상. DB 구현체 변경에 유연하게 대응 가능
 */
public interface UserReposiory extends JpaRepository<UserEntity, Long> {
    // 사용자 아이디로 DB에서 사용자 조회
    Optional<UserEntity> findByUserId(String userId);

    // 사용자 아이디로 DB에서 사용자 삭제
    void deleteByUserId(String userId);
}
