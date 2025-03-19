package kr.daeho.AssetAssistant.assets.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import kr.daeho.AssetAssistant.assets.vo.*;

/**
 * 자산 정보를 저장하는 엔티티 클래스
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
@Table(name = "assets", indexes = {
        @Index(name = "idx_user_id_assets", columnList = "user_id"),
        @Index(name = "idx_total_assets", columnList = "total_assets")
})
public class AssetsEntity {
    /**
     * 자산 데이터 고유 식별자 (Assets DB의 PK)
     * 
     * @Id: 기본 키 지정
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
     * 총 자산
     * 
     * @Column: 테이블의 컬럼과 매핑
     *          - name: 컬럼명
     *          - nullable: 널 가능 여부
     *          - unique: 유니크 제약 조건 여부
     */
    @Column(name = "total_assets", nullable = false)
    private int totalAssets;

    /**
     * 수입 대비 지출 비율
     * 
     * @Column: 테이블의 컬럼과 매핑
     *          - name: 컬럼명
     *          - nullable: 널 가능 여부
     */
    @Column(name = "income_expense_ratio", nullable = false)
    private double incomeExpenseRatio;

    /**
     * 총 수입 금액
     */
    @Column(name = "total_income", nullable = false)
    private int totalIncome;

    /**
     * 총 지출 금액
     */
    @Column(name = "total_expense", nullable = false)
    private int totalExpense;

    /**
     * 수입 정보 목록
     * 
     * @ElementCollection(fetch = FetchType.EAGER): 값 타입 컬렉션을 매핑
     *                          - 컬렉션을 즉시 로딩하여 N+1 문제 방지
     *                          - 자산 비율 정보는 항상 함께 조회되므로 EAGER 로딩이 효율적
     *                          - EAGER: 엔티티를 조회할 때, 해당 컬렉션도 함께 즉시 로딩
     *                          - Lazy: 엔티티를 조회할 때, 해당 컬렉션을 지연 로딩
     * @CollectionTable: 엔티티 클래스의 컬렉션 필드를 별도의 테이블에 저장
     *                   - joinColumns: 조인 조건 정의
     *                   - name: 이 테이블에 있는 외래 키 컬럼의 이름
     *                   - referencedColumnName: 메인 테이블에서 참조할 컬럼 이름
     * @Builder.Default: 빌더 패턴 사용 시, 기본값 지정 (null 대신 빈 컬렉션으로 초기화)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "incomes", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"))
    @Builder.Default
    private List<IncomeVo> incomes = new ArrayList<>();

    /**
     * 지출 정보 목록
     * 
     * @ElementCollection(fetch = FetchType.EAGER): 값 타입 컬렉션을 매핑
     *                          - 컬렉션을 즉시 로딩하여 N+1 문제 방지
     *                          - 자산 비율 정보는 항상 함께 조회되므로 EAGER 로딩이 효율적
     *                          - EAGER: 엔티티를 조회할 때, 해당 컬렉션도 함께 즉시 로딩
     *                          - Lazy: 엔티티를 조회할 때, 해당 컬렉션을 지연 로딩
     * @CollectionTable: 엔티티 클래스의 컬렉션 필드를 별도의 테이블에 저장
     *                   - joinColumns: 조인 조건 정의
     *                   - name: 이 테이블에 있는 외래 키 컬럼의 이름
     *                   - referencedColumnName: 메인 테이블에서 참조할 컬럼 이름
     * @Builder.Default: 빌더 패턴 사용 시, 기본값 지정 (null 대신 빈 컬렉션으로 초기화)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expenses", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"))
    @Builder.Default
    private List<ExpenseVo> expenses = new ArrayList<>();

    /**
     * 자산 유형별 비율을 저장하는 Map 컬렉션
     * 
     * @ElementCollection(fetch = FetchType.EAGER): 값 타입 컬렉션을 매핑
     *                          - 컬렉션을 즉시 로딩하여 N+1 문제 방지
     *                          - 자산 비율 정보는 항상 함께 조회되므로 EAGER 로딩이 효율적
     *                          - EAGER: 엔티티를 조회할 때, 해당 컬렉션도 함께 즉시 로딩
     *                          - Lazy: 엔티티를 조회할 때, 해당 컬렉션을 지연 로딩
     * @CollectionTable: 엔티티 클래스의 컬렉션 필드를 별도의 테이블에 저장
     *                   - joinColumns: 조인 조건 정의
     *                   - name: 이 테이블에 있는 외래 키 컬럼의 이름
     *                   - referencedColumnName: 메인 테이블에서 참조할 컬럼 이름
     *                   - uniqueConstraints: 유니크 제약조건 정의
     *                   -> user_id, asset_type: 동일한 사용자가 동일한 자산 타입 등록 방지
     * @MapKeyColumn: Map의 key에 대한 컬럼 지정
     * @Column: Map의 value에 대한 컬럼 지정
     * @Builder.Default: 빌더 패턴 사용 시, 기본값 지정 (null 대신 빈 컬렉션으로 초기화)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assets_type_ratios", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"), uniqueConstraints = @UniqueConstraint(columnNames = {
            "user_id", "asset_type" }))
    @MapKeyColumn(name = "asset_type")
    @Column(name = "ratio")
    @Builder.Default
    private Map<String, Double> assetsTypeRatios = new HashMap<>();

    /**
     * 자산 상세 정보 목록
     * 
     * @ElementCollection(fetch = FetchType.EAGER): 값 타입 컬렉션을 매핑
     *                          - 컬렉션을 즉시 로딩하여 N+1 문제 방지
     *                          - 자산 비율 정보는 항상 함께 조회되므로 EAGER 로딩이 효율적
     *                          - EAGER: 엔티티를 조회할 때, 해당 컬렉션도 함께 즉시 로딩
     *                          - Lazy: 엔티티를 조회할 때, 해당 컬렉션을 지연 로딩
     * @CollectionTable: 엔티티 클래스의 컬렉션 필드를 별도의 테이블에 저장
     *                   - joinColumns: 조인 조건 정의
     *                   - name: 이 테이블에 있는 외래 키 컬럼의 이름
     *                   - referencedColumnName: 메인 테이블에서 참조할 컬럼 이름
     *                   - uniqueConstraints: 유니크 제약조건 정의
     *                   -> user_id, assets_name: 동일한 사용자가 동일한 자산 이름 등록 방지
     * @Builder.Default: 빌더 패턴 사용 시, 기본값 지정 (null 대신 빈 컬렉션으로 초기화)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assets_details", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"), uniqueConstraints = @UniqueConstraint(columnNames = {
            "user_id", "assets_name" }))
    @Builder.Default
    private List<AssetsDetailVo> assetDetails = new ArrayList<>();

    /**
     * 자산 정보 생성일
     * 
     * @CreationTimestamp: 엔티티 생성 시 자동으로 현재 시간 설정
     * @Column: 테이블의 컬럼과 매핑
     *          - name: 컬럼명
     *          - nullable: 널 가능 여부
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 자산 정보 수정일
     * 
     * @UpdateTimestamp: 엔티티 수정 시 자동으로 현재 시간 설정
     * @Column: 테이블의 컬럼과 매핑
     *          - name: 컬럼명
     *          - nullable: 널 가능 여부
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 업데이트
    // 엔티티에서 선언한 객체 업데이트 메소드 (불변 객체가 아니므로, 바로 해당 값을 전달해 업데이트)

    /**
     * 총 자산 업데이트 메소드
     * 
     * @param totalAssets 업데이트할 총 자산
     */
    public void updateTotalAssets(int totalAssets) {
        this.totalAssets = totalAssets;
    }

    /**
     * 수입 대비 지출 비율 업데이트 메소드
     * 
     * @param incomeExpenseRatio 업데이트할 수입 대비 지출 비율
     */
    public void updateIncomeExpenseRatio(double incomeExpenseRatio) {
        this.incomeExpenseRatio = incomeExpenseRatio;
    }

    // VO 객체 업데이트 메소드 (불변 객체이므로, 새로운 인스턴스를 전달 (IncomeVo, ExpenseVo, AssetsDetailVo))

    /**
     * 수입 목록 업데이트 메소드
     * 
     * @param incomes 업데이트할 수입 목록
     */
    public void updateIncomes(List<IncomeVo> incomes) {
        this.incomes.clear();
        if (incomes != null) {
            this.incomes.addAll(incomes);
        }
    }

    /**
     * 지출 목록 업데이트 메소드
     * 
     * @param expenses 업데이트할 지출 목록
     */
    public void updateExpenses(List<ExpenseVo> expenses) {
        this.expenses.clear();
        if (expenses != null) {
            this.expenses.addAll(expenses);
        }
    }

    /**
     * 자산 유형별 비율 업데이트 메소드
     * 
     * @param assetsTypeRatios 업데이트할 자산 유형별 비율 Map
     */
    public void updateAssetsTypeRatios(Map<String, Double> assetsTypeRatios) {
        this.assetsTypeRatios.clear();
        this.assetsTypeRatios.putAll(assetsTypeRatios);
    }

    /**
     * 자산 상세 정보 목록 업데이트 메소드
     * 
     * @param assetDetails 업데이트할 자산 상세 정보 목록
     */
    public void updateAssetDetails(List<AssetsDetailVo> assetDetails) {
        this.assetDetails.clear();
        this.assetDetails.addAll(assetDetails);
    }

    /**
     * 총 수입 업데이트 메소드
     * 
     * @param totalIncome 업데이트할 총 수입 금액
     */
    public void updateTotalIncome(int totalIncome) {
        this.totalIncome = totalIncome;
    }

    /**
     * 총 지출 업데이트 메소드
     * 
     * @param totalExpense 업데이트할 총 지출 금액
     */
    public void updateTotalExpense(int totalExpense) {
        this.totalExpense = totalExpense;
    }
}
