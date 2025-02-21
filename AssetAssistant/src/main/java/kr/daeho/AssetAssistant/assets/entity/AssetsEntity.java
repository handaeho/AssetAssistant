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
     * 수입 정보 -> VO 객체를 사용하여 Embedded
     * 
     * @Embedded: 임베디드 타입 선언
     * @AttributeOverrides: 임베디드 타입의 컬럼 매핑 재정의
     *                      - Embedded로 다른 객체를 필드에 선언한 경우, 해당 엔티티에서는 다른 컬럼명을 쓰고 싶을 때
     * @AttributeOverride: name = "부모클래스 필드명", column = @Column(name = "자식에서 쓸 컬럼명")
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "incomeName", column = @Column(name = "income_name", nullable = false)),
            @AttributeOverride(name = "incomeType", column = @Column(name = "income_type", nullable = false)),
            @AttributeOverride(name = "incomeAmount", column = @Column(name = "income_amount", nullable = false))
    })
    private IncomeVo income;

    /**
     * 지출 정보 -> VO 객체를 사용하여 Embedded
     * 
     * @Embedded: 임베디드 타입 선언
     * @AttributeOverrides: 임베디드 타입의 컬럼 매핑 재정의
     *                      - Embedded로 다른 객체를 필드에 선언한 경우, 해당 엔티티에서는 다른 컬럼명을 쓰고 싶을 때
     * @AttributeOverride: name = "부모클래스 필드명", column = @Column(name = "자식에서 쓸 컬럼명")
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fixedExpenseName", column = @Column(name = "fixed_expense_name", nullable = false)),
            @AttributeOverride(name = "fixedExpenseType", column = @Column(name = "fixed_expense_type", nullable = false)),
            @AttributeOverride(name = "fixedExpenseAmount", column = @Column(name = "fixed_expense_amount", nullable = false)),
            @AttributeOverride(name = "variableExpenseName", column = @Column(name = "variable_expense_name", nullable = false)),
            @AttributeOverride(name = "variableExpenseType", column = @Column(name = "variable_expense_type", nullable = false)),
            @AttributeOverride(name = "variableExpenseAmount", column = @Column(name = "variable_expense_amount", nullable = false))
    })
    private ExpenseVo expense;

    /**
     * 자산 유형별 비율을 저장하는 Map 컬렉션
     * 
     * @ElementCollection(fetch = FetchType.EAGER): 값 타입 컬렉션을 매핑
     *                          - 컬렉션을 즉시 로딩하여 N+1 문제 방지
     *                          - 자산 비율 정보는 항상 함께 조회되므로 EAGER 로딩이 효율적
     * @CollectionTable: 별도 테이블로 저장하여 유연한 확장성 확보
     *                   - 자산 타입이 추가되어도 엔티티 구조 변경 불필요
     *                   - name: 테이블 이름 지정
     *                   - joinColumns: 외래 키로 사용할 컬럼 지정
     * @MapKeyColumn: Map의 key에 대한 컬럼 지정
     * @Column: Map의 value에 대한 컬럼 지정
     * @Builder.Default: 빌더 패턴 사용 시, 기본값 지정 (null 대신 빈 컬렉션으로 초기화)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assets_type_ratios", joinColumns = @JoinColumn(name = "assets_id"))
    @MapKeyColumn(name = "asset_type")
    @Column(name = "ratio")
    @Builder.Default
    private Map<String, Double> assetsTypeRatios = new HashMap<>();

    /**
     * 자산 상세 정보 목록
     * 
     * @ElementCollection(fetch = FetchType.EAGER): 값 타입 컬렉션을 매핑
     *                          - 컬렉션을 즉시 로딩하여 N+1 문제 방지
     *                          - 자산 상세 정보는 항상 함께 조회되므로 EAGER 로딩이 효율적
     * @CollectionTable: 별도 테이블로 저장하여 유연한 확장성 확보
     *                   - 자산 타입이 추가되어도 엔티티 구조 변경 불필요
     *                   - name: 테이블 이름 지정
     *                   - joinColumns: 외래 키로 사용할 컬럼 지정
     * @Builder.Default: 빌더 패턴 사용 시, 기본값 지정 (null 대신 빈 컬렉션으로 초기화)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assets_details", joinColumns = @JoinColumn(name = "assets_id"))
    @Builder.Default
    private List<AssetsDetailVo> assetDetails = new ArrayList<>();

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
     * 수입 업데이트 메소드
     * 
     * @param income 업데이트할 수입
     */
    public void updateIncome(IncomeVo income) {
        this.income = income;
    }

    /**
     * 지출 업데이트 메소드
     * 
     * @param expense 업데이트할 지출
     */
    public void updateExpense(ExpenseVo expense) {
        this.expense = expense;
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
}
