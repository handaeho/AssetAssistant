package kr.daeho.AssetAssistant.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import kr.daeho.AssetAssistant.users.entity.UserEntity;

/**
 * 사용자 레파지토리
 * 
 * 사용자 정보의 생성, 조회, 수정, 삭제 등 영속성 계층 연산을 제공
 * 
 * 스프링 데이터 JPA를 통해 자동으로 구현체가 생성됨
 * 
 * 클라이언트 <-> 컨트롤러 <-> 인터페이스 <-> 서비스 <-> 레파지토리 <-> DB
 * 
 * 레파지토리의 주요 역할: DB와의 직접적인 상호작용 및 데이터 영속성 유지
 * -> 데이터 접근 로직을 한 곳에서 관리하여 코드 중복 방지
 * -> DB 연산을 추상화하여 비즈니스 로직과 분리
 * -> 테스트 용이성 향상. DB 구현체 변경에 유연하게 대응 가능
 */
public interface UserReposiory extends JpaRepository<UserEntity, Long> {
    /**
     * 사용자 아이디로 사용자 정보 조회 (조회 결과가 없을 경우 Optional.empty 반환(NPE 방지))
     * 
     * @param userId 사용자 아이디
     * @return Optional<UserEntity> 조회된 사용자 정보
     */
    Optional<UserEntity> findByUserId(String userId);

    /**
     * 사용자 아이디 존재 여부 확인 (회원가입 시 아이디 중복 검사)
     * 
     * @param userId 사용자 아이디
     * @return boolean 존재 여부
     */
    boolean existsByUserId(String userId);

    /**
     * 사용자 아이디로 DB에서 사용자 삭제
     * 
     * @param userId 사용자 아이디
     */
    void deleteByUserId(String userId);
}
