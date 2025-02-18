package kr.daeho.AssetAssistant.assets.dto;

import kr.daeho.AssetAssistant.assets.entity.AssetsEntity;
import kr.daeho.AssetAssistant.assets.vo.IncomeVo;
import kr.daeho.AssetAssistant.assets.vo.ExpenseVo;
import kr.daeho.AssetAssistant.assets.vo.AssetsDetailVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자산 정보 DTO
 * 
 * DTO: 클라이언트와 서비스 간 데이터 전송을 위한 객체
 * 
 * Entity와 비슷하지만, 외부 요청 및 응답에 필요한 데이터만 포함.
 * 
 * DTO는 컨트롤러와 서비스 계층에서 엔티티의 변경을 직접적으로 노출하지 않고, 필요한 데이터만 전송.
 * 
 * @Getter: 모든 필드의 Getter 메서드를 자동으로 생성. setter는 의도적으로 제외하여 불변성 유지
 * @Builder: 빌더 패턴 사용. 객체 생성 후 setter는 제외하여 불변성 유지
 * @NoArgsConstructor: 기본 생성자를 자동으로 생성. JPA Entity에 필수
 * @AllArgsConstructor: 모든 필드를 매개변수로 하는 생성자를 자동으로 생성
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetsDto {
    private Long id;
    private int totalAssets;
    private IncomeVo income; // VO 객체를 사용
    private ExpenseVo expense; // VO 객체를 사용
    private AssetsDetailVo assetDetail; // VO 객체를 사용
    private double incomeExpenseRatio;
    private double assetsRatio;

    // AssetsDto를 AssetsEntity로 변환
    // DTO를 Entity로 변환할 때는 특정 DTO 인스턴스의 현재 상태(this)가 필요하므로 인스턴스 메서드로 구현
    public AssetsEntity toAssetsEntity() {
        return AssetsEntity.builder()
                .totalAssets(this.totalAssets)
                .income(this.income)
                .expense(this.expense)
                .assetDetail(this.assetDetail)
                .incomeExpenseRatio(this.incomeExpenseRatio)
                .assetsRatio(this.assetsRatio)
                .build();
    }

    // AssetsEntity를 AssetsDto로 변환
    // Entity를 DTO로 변환할 때는 특정 DTO 인스턴스의 상태가 필요하지 않으므로 static 메서드로 구현
    public static AssetsDto fromAssetsEntity(AssetsEntity entity) {
        return AssetsDto.builder()
                .id(entity.getId())
                .totalAssets(entity.getTotalAssets())
                .income(entity.getIncome())
                .expense(entity.getExpense())
                .assetDetail(entity.getAssetDetail())
                .incomeExpenseRatio(entity.getIncomeExpenseRatio())
                .assetsRatio(entity.getAssetsRatio())
                .build();
    }
}
