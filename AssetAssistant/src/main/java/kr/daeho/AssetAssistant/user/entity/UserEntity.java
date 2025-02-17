package kr.daeho.AssetAssistant.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보 Entity
 * 
 * Entity: 데이터베이스와 직접 연결되는 객체
 * 
 * 
 * 
 * @Getter: 모든 필드의 Getter 메서드를 자동으로 생성. setter는 의도적으로 제외하여 불변성 유지
 * @NoArgsConstructor: 기본 생성자를 자동으로 생성
 * @AllArgsConstructor: 모든 필드를 매개변수로 하는 생성자를 자동으로 생성
 * @Builder: 빌더 패턴을 사용할 수 있게 해줌
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    // 사용자 데이터 고유 식별자(User DB의 PK)
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    // 사용자 아이디
    private String userId;

    // 사용자 이름
    private String userName;

    // 사용자 비밀번호
    private String userPassword;

    // 사용자 나이
    private int userAge;

    // 사용자 직업
    private String userJob;

    // 사용자 이름 정보 업데이트
    public void updateUserName(String userName) {
        this.userName = userName;
    }

    // 사용자 비밀번호 업데이트
    public void updateUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    // 사용자 나이 업데이트
    public void updateUserAge(int userAge) {
        this.userAge = userAge;
    }

    // 사용자 직업 업데이트
    public void updateUserJob(String userJob) {
        this.userJob = userJob;
    }
}
