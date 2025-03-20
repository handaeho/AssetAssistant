package kr.daeho.AssetAssistant.users.entity;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import kr.daeho.AssetAssistant.users.enums.UserRoleEnum;

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
public class UserEntity implements UserDetails {
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
     * 사용자 이름
     * 
     * @Column: 테이블의 컬럼과 매핑
     *          - name: 컬럼명
     *          - nullable: 널 가능 여부
     */
    @Column(name = "user_name", nullable = false)
    private String userName;

    /**
     * 사용자 나이
     * 
     * @Column: 테이블의 컬럼과 매핑
     *          - name: 컬럼명
     *          - nullable: 널 가능 여부
     * @Default: 빌더 패턴에서 기본값 설정
     */
    @Column(name = "user_age", nullable = true)
    @Default
    private Integer userAge = 0;

    /**
     * 사용자 생년월일
     * 
     * @Column: 테이블의 컬럼과 매핑
     *          - name: 컬럼명
     *          - nullable: 널 가능 여부
     */
    @Column(name = "user_birth_date", nullable = true)
    private LocalDate userBirthDate;

    /**
     * 사용자 직업
     * 
     * @Column: 테이블의 컬럼과 매핑
     *          - name: 컬럼명
     *          - nullable: 널 가능 여부
     */
    @Column(name = "user_job", nullable = true)
    private String userJob;

    /**
     * 사용자 정보 수정일
     * 
     * @UpdateTimestamp: 엔티티 수정 시 자동으로 수정일 업데이트
     * @Column: 테이블의 컬럼과 매핑
     *          - name: 컬럼명
     *          - nullable: 널 가능 여부
     */
    @UpdateTimestamp
    @Column(name = "user_updated_at", nullable = false)
    private LocalDateTime userUpdatedAt;

    /**
     * 사용자 역할(권한, role)
     * 
     * @Enumerated: 열거형(Enum) 타입 매핑
     *              - EnumType.STRING: 열거형 타입의 이름을 문자열로 저장
     * @Column: 테이블의 컬럼과 매핑
     *          - length: 컬럼 길이 지정
     * @Default: 빌더 패턴에서 기본값 설정
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Default
    private UserRoleEnum role = UserRoleEnum.USER;

    /**
     * 사용자 이름 정보 업데이트
     * 
     * @param userName: 업데이트할 사용자 이름
     */
    public void updateUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 사용자 비밀번호 정보 업데이트
     * 
     * @param userPassword: 업데이트할 사용자 비밀번호
     */
    public void updateUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * 사용자 직업 업데이트
     * 
     * @param userJob: 업데이트할 사용자 직업
     */
    public void updateUserJob(String userJob) {
        this.userJob = userJob;
    }

    /**
     * 사용자 나이 업데이트
     * 
     * @param userAge: 업데이트할 사용자 나이
     */
    public void updateUserAge(Integer userAge) {
        this.userAge = userAge;
    }

    /**
     * 사용자 생년월일 설정 및 나이 자동 계산
     * 
     * @param userBirthDate: 설정할 사용자 생년월일
     */
    public void setUserBirthDate(LocalDate userBirthDate) {
        this.userBirthDate = userBirthDate;
        calculateAndUpdateAge();
    }

    /**
     * 생년월일을 기반으로 나이 자동 계산 및 업데이트
     */
    private void calculateAndUpdateAge() {
        try {
            if (this.userBirthDate != null) {
                LocalDate today = LocalDate.now();
                int currentYear = today.getYear();
                int birthYear = this.userBirthDate.getYear();

                // 유효하지 않은 생년월일 체크 (미래 날짜)
                if (this.userBirthDate.isAfter(today)) {
                    this.userAge = 0; // 기본값으로 설정
                    return;
                }

                // 기본 나이 계산 (현재 연도 - 생년 연도)
                this.userAge = currentYear - birthYear;

                // 생일이 지나지 않았으면 나이 1살 차감
                if (today.getDayOfYear() < this.userBirthDate.withYear(currentYear).getDayOfYear()) {
                    this.userAge--;
                }

                // 너무 오래된 생년월일이나 음수 나이 방지
                if (this.userAge < 0 || this.userAge > 150) {
                    this.userAge = 0; // 기본값으로 설정
                }
            } else {
                // 생년월일이 없으면 나이도 null로 설정
                this.userAge = null;
            }
        } catch (Exception e) {
            // 예외 발생 시 기본값 설정
            this.userAge = 0;
        }
    }

    // UserDetails 인터페이스 구현부

    /**
     * Spring Security의 UserDetails의 getAuthorities 메소드 구현
     * 
     * Collection<? extends GrantedAuthority>: GrantedAuthority 인터페이스를 구현한 객체들의 컬렉션
     * 
     * Collection: List, Queue, Set, Map 등을 포함하는 자료구조 인터페이스
     * 
     * List.of(): 주어진 요소를 포함하는 불변(immutable) 리스트를 생성
     * 
     * SimpleGrantedAuthority: Spring Security의 GrantedAuthority 구현체 (문자열 형태의 권한 정보)
     * 
     * role.name(): 사용자 역할을 문자열로 반환 (Enum 타입의 이름)
     * 
     * @return 사용자에게 부여된 권한(roles, authorities)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public String getPassword() {
        return userPassword;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
