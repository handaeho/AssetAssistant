package kr.daeho.AssetAssistant.assets.dto;

import kr.daeho.AssetAssistant.assets.entity.AssetsEntity;
import kr.daeho.AssetAssistant.assets.vo.IncomeVo;
import kr.daeho.AssetAssistant.assets.vo.ExpenseVo;
import kr.daeho.AssetAssistant.assets.vo.AssetsDetailVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * 자산 정보 DTO
 * 
 * DTO: 클라이언트와 서비스 간 데이터 전송을 위한 객체
 * 
 * Entity와 비슷하지만, 클라이언트 요청 및 응답에 필요한 데이터만 포함.
 * 
 * DTO는 컨트롤러와 서비스 계층에서 엔티티의 변경을 직접적으로 노출하지 않고, 필요한 데이터만 클라이언트에게 전송.
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
    // DB에서 사용되는 고유식별자(ID)
    private Long id;

    // 사용자 식별자
    private String userId;

    // 전체 자산 금액 (예: 100만원)
    private int totalAssets;

    // 수입 정보 VO (불변 객체로, 수정 시 새 인스턴스 생성)
    private IncomeVo income;

    // 지출 정보 VO (불변 객체로, 수정 시 새 인스턴스 생성)
    private ExpenseVo expense;

    /**
     * 여러 자산 상세 정보를 저장하는 리스트
     * 각 자산 상세 정보는 자산의 타입, 이름, 금액 등을 포함
     */
    private List<AssetsDetailVo> assetDetails;

    /**
     * 각 자산 타입별 비율을 저장하는 Map
     * 예: {"주식": 50.0, "적금": 50.0}
     */
    private Map<String, Double> assetsTypeRatios;

    // 계산된 수입 대비 지출 비율 (백분율)
    private double incomeExpenseRatio;

    /**
     * AssetsDto를 AssetsEntity로 변환하는 메소드
     * 
     * 신규 생성이라면, DB에서 필요한 모든 필드 값을 빌드해야 하고 (기본 값, 자동 생성 값 등 포함)
     * 업데이트라면, 업데이트 할 필드만 빌드하면 나머지는 DB에 있는 기존 값을 유지하게 가능
     * DTO를 Entity로 변환할 때는 특정 DTO 인스턴스의 현재 상태(this)가 필요하므로 인스턴스 메서드로 구현
     * 
     * @return 변환된 AssetsEntity 객체
     */
    public AssetsEntity toAssetsEntity() {
        return AssetsEntity.builder()
                .id(this.id)
                .userId(this.userId)
                .totalAssets(this.totalAssets)
                .income(this.income)
                .expense(this.expense)
                .assetDetails(this.assetDetails)
                .assetsTypeRatios(this.assetsTypeRatios)
                .incomeExpenseRatio(this.incomeExpenseRatio)
                .build();
    }

    /**
     * AssetsEntity를 AssetsDto로 변환하는 메소드
     * 
     * DB에서 클라이언트에게 전달할 데이터만 빌드
     * Entity를 DTO로 변환할 때는 특정 DTO 인스턴스의 상태가 필요하지 않으므로 static 메서드로 구현
     * 
     * @param entity 변환할 엔티티
     * @return 변환된 DTO
     */
    public static AssetsDto fromAssetsEntity(AssetsEntity entity) {
        return AssetsDto.builder()
                .userId(entity.getUserId())
                .totalAssets(entity.getTotalAssets())
                .income(entity.getIncome())
                .expense(entity.getExpense())
                .assetDetails(entity.getAssetDetails())
                .assetsTypeRatios(entity.getAssetsTypeRatios())
                .incomeExpenseRatio(entity.getIncomeExpenseRatio())
                .build();
    }
}
