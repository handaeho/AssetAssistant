package kr.daeho.AssetAssistant.auth.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.daeho.AssetAssistant.users.entity.UserEntity;

/**
 * 사용자 인증 정보 Entity
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
@Table(name = "auth", indexes = {
        @Index(name = "idx_user_id_auth", columnList = "user_id")
})
public class AuthEntity {
    /**
     * 사용자 데이터 고유 식별자 (User DB의 PK)
     * 
     * @Id: 해당 필드가 테이블의 PK임을 명시
     * @GeneratedValue: 기본 키 자동 생성 (GenerationType.IDENTITY: 기본 키 자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 아이디
     * 
     * @Column: 테이블의 컬럼과 매핑
     *          - name: 컬럼명
     *          - nullable: 널 가능 여부
     *          - unique: 유니크 제약 조건 여부
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    /**
     * 사용자 비밀번호
     * 
     * @Column: 테이블의 컬럼과 매핑
     *          - name: 컬럼명
     *          - nullable: 널 가능 여부
     */
    @Column(name = "user_password", nullable = false)
    private String userPassword;

    /**
     * 사용자 권한
     * 
     * @Column: 테이블의 컬럼과 매핑
     *          - name: 컬럼명
     *          - nullable: 널 가능 여부
     */
    @Column(name = "user_role", nullable = false)
    private String userRole;

    /**
     * 계정 잠금 여부
     */
    @Builder.Default
    @Column(name = "locked", nullable = false)
    private boolean locked = false;

    /**
     * 비밀번호 만료 일시
     */
    @Column(name = "password_expiry_date", nullable = false)
    private LocalDateTime passwordExpiryDate;

    /**
     * 최종 로그인 일시
     */
    @Column(name = "last_login_date", nullable = false)
    private LocalDateTime lastLoginDate;

    /**
     * 계정 생성 일시
     */
    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 계정 업데이트 일시
     */
    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 사용자 프로필 정보 (users 테이블 정보)
     * 
     * @OneToOne: 1:1 관계 설정
     * @FetchType.LAZY: 지연 로딩 설정 (실제로 사용하는 시점에 조회)
     * @JoinColumn: 조인 컬럼 지정
     *              - name: 조인 컬럼명
     *              - referencedColumnName: 참조 컬럼명
     *              - insertable: 삽입 가능 여부
     *              - updatable: 수정 가능 여부
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private UserEntity userProfile;

    /**
     * 비밀번호 업데이트
     * 
     * @param newPassword 새 암호화된 비밀번호
     */
    public void updatePassword(String newPassword) {
        this.userPassword = newPassword;
        this.passwordExpiryDate = LocalDateTime.now().plusMonths(3); // 비밀번호 유효기간 3개월
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 로그인 성공 시 호출
     * 마지막 로그인 시간 업데이트
     */
    public void updateLoginTime() {
        this.lastLoginDate = LocalDateTime.now();
    }

    /**
     * 계정 잠금/해제
     * 
     * @param locked 잠금 여부
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
        this.updatedAt = LocalDateTime.now();
    }
}
