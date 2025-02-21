package kr.daeho.AssetAssistant.users.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보 Entity
 * 
 * DB 테이블과 1:1 또는 1:N 관계로 매핑되는 객체로 DB에 저장되거나 조회되는 데이터의 단위
 * 
 * Entity는 DTO와 달리, 비즈니스 로직이나 연관관계 관리 메소드 등을 가질 수 있음
 * 
 * @Getter: 모든 필드의 Getter 메서드를 자동으로 생성. setter는 의도적으로 제외하여 불변성 유지
 * @Builder: 빌더 패턴 사용. 객체 생성 후 setter는 제외하여 불변성 유지
 * @NoArgsConstructor: 기본 생성자를 자동으로 생성. JPA Entity에 필수
 * @AllArgsConstructor: 모든 필드를 매개변수로 하는 생성자를 자동으로 생성
 * @Table: 엔티티와 매핑할 테이블 지정 (name: 테이블 이름, indexes: 인덱스 정보)
 * @Index: 테이블에 인덱스 생성 (name: 인덱스 이름, columnList: 적용 컬럼) -
 *         - 각 엔티티에서 인덱스 이름은 스키마 내에서 유일해야 함
 *         - e.g. 같은 user_id에 대해 assets와 users 테이블의 인덱스
 *         -> idx_user_id_assets, idx_user_id_users
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_user_id_users", columnList = "user_id")
})
public class UserEntity {
    // 사용자 데이터 고유 식별자 (User DB의 PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 아이디
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    // 사용자 이름
    @Column(name = "user_name", nullable = false)
    private String userName;

    // 사용자 비밀번호
    @Column(name = "user_password", nullable = false)
    private String userPassword;

    // 사용자 나이
    @Column(name = "user_age", nullable = true)
    private int userAge;

    // 사용자 직업
    @Column(name = "user_job", nullable = true)
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
