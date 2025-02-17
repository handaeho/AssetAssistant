package kr.daeho.AssetAssistant.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kr.daeho.AssetAssistant.user.entity.UserEntity;

public interface UserReposiory extends JpaRepository<UserEntity, Long> {    
    // 사용자 아이디로 DB에서 사용자 조회
    public UserEntity findByUserId(String userId);
}
