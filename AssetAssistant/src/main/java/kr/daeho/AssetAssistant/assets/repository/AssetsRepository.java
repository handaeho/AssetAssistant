package kr.daeho.AssetAssistant.assets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import kr.daeho.AssetAssistant.assets.entity.AssetsEntity;

/**
 * 자산 레파지토리
 * 
 * 클라이언트 <-> 컨트롤러 <-> 인터페이스 <-> 서비스 <-> 레파지토리 <-> DB
 * 
 * 레파지토리의 주요 역할: DB와의 직접적인 상호작용. 데이터 영속성 유지.
 * 
 * 데이터 접근 로직을 한 곳에서 관리하여 코드 중복 방지
 * DB 연산을 추상화하여 비즈니스 로직과 분리
 * 테스트 용이성 향상. DB 구현체 변경에 유연하게 대응 가능
 */
public interface AssetsRepository extends JpaRepository<AssetsEntity, Long> {
    // 사용자 아이디로 자산 정보 검색
    Optional<AssetsEntity> findByUserId(String userId);

    // 사용자 아이디로 자산 정보 삭제
    void deleteByUserId(String userId);
}
