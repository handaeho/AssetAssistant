package kr.daeho.AssetAssistant.assets.entity;

import jakarta.persistence.*;
import kr.daeho.AssetAssistant.assets.vo.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자산 정보 Entity
 * 
 * DB 테이블과 1:1 또는 1:N 관계로 매핑되는 객체로 DB에 저장되거나 조회되는 데이터의 단위
 * 
 * Entity는 DTO와 달리, 비즈니스 로직이나 연관관계 관리 메소드 등을 가질 수 있음음
 * 
 * @Getter: 모든 필드의 Getter 메서드를 자동으로 생성. setter는 의도적으로 제외하여 불변성 유지
 * @Builder: 빌더 패턴 사용. 객체 생성 후 setter는 제외하여 불변성 유지
 * @NoArgsConstructor: 기본 생성자를 자동으로 생성. JPA Entity에 필수
 * @AllArgsConstructor: 모든 필드를 매개변수로 하는 생성자를 자동으로 생성
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assets", indexes = {
        // 자주 검색되는 자산 타입과 자산 이름으로 DB 인덱스 생성 (인덱스 이름, 적용 컬럼)
        @Index(name = "idx_assets_type", columnList = "assets_type"),
        @Index(name = "idx_assets_name", columnList = "assets_name")
})
public class AssetsEntity {
    // 자산 데이터 고유 식별자 (Assets DB의 PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 총 자산
    @Column(name = "total_assets", nullable = false)
    private int totalAssets;

    // 수입 정보 -> VO 객체를 사용하여 Embedded
    @Embedded
    // @Embedded를 통해 다른 객체를 필드에 선언한 경우, 해당 엔티티에서는 다른 컬럼명을 사용하고 싶을 때
    // @AttributeOverride(name = "부모클래스 필드명", column = @Column(name = "자식에서 쓸 컬럼명"))
    @AttributeOverrides({
            @AttributeOverride(name = "incomeName", column = @Column(name = "income_name", nullable = false)),
            @AttributeOverride(name = "incomeType", column = @Column(name = "income_type", nullable = false)),
            @AttributeOverride(name = "incomeAmount", column = @Column(name = "income_amount", nullable = false))
    })
    private IncomeVo income;

    // 지출 정보 -> VO 객체를 사용하여 Embedded
    @Embedded
    // @Embedded를 통해 다른 객체를 필드에 선언한 경우, 해당 엔티티에서는 다른 컬럼명을 사용하고 싶을 때
    // @AttributeOverride(name = "부모클래스 필드명", column = @Column(name = "자식에서 쓸 컬럼명"))
    @AttributeOverrides({
            @AttributeOverride(name = "fixedExpenseName", column = @Column(name = "fixed_expense_name", nullable = false)),
            @AttributeOverride(name = "fixedExpenseType", column = @Column(name = "fixed_expense_type", nullable = false)),
            @AttributeOverride(name = "fixedExpenseAmount", column = @Column(name = "fixed_expense_amount", nullable = false)),
            @AttributeOverride(name = "variableExpenseName", column = @Column(name = "variable_expense_name", nullable = false)),
            @AttributeOverride(name = "variableExpenseType", column = @Column(name = "variable_expense_type", nullable = false)),
            @AttributeOverride(name = "variableExpenseAmount", column = @Column(name = "variable_expense_amount", nullable = false))
    })
    private ExpenseVo expense;

    // 자산 상세 정보 -> VO 객체를 사용하여 Embedded
    @Embedded
    // @Embedded를 통해 다른 객체를 필드에 선언한 경우, 해당 엔티티에서는 다른 컬럼명을 사용하고 싶을 때
    // @AttributeOverride(name = "부모클래스 필드명", column = @Column(name = "자식에서 쓸 컬럼명"))
    @AttributeOverrides({
            @AttributeOverride(name = "assetsName", column = @Column(name = "assets_name", nullable = false)),
            @AttributeOverride(name = "assetsType", column = @Column(name = "assets_type", nullable = false)),
            @AttributeOverride(name = "assetsPrice", column = @Column(name = "assets_price", nullable = false))
    })
    private AssetsDetailVo assetDetail;

    // 수입 대비 지출 비율
    @Column(name = "income_expense_ratio", nullable = false)
    private double incomeExpenseRatio;

    // 자산 유형별 비율
    @Column(name = "assets_ratio", nullable = false)
    private double assetsRatio;

    // 업데이트 -> VO 객체를 업데이트 하기 위해 새로운 값을 생성하는 메소드 (서비스 레이어에서 업데이트)
    public void updateIncome(IncomeVo income) {
        this.income = income;
    }

    public void updateExpense(ExpenseVo expense) {
        this.expense = expense;
    }

    public void updateAssetDetail(AssetsDetailVo assetDetail) {
        this.assetDetail = assetDetail;
    }

    public void updateRatios(double incomeExpenseRatio, double assetsRatio) {
        this.incomeExpenseRatio = incomeExpenseRatio;
        this.assetsRatio = assetsRatio;
    }
}
