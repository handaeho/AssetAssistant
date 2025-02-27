package kr.daeho.AssetAssistant.assets.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

import kr.daeho.AssetAssistant.assets.vo.IncomeVo;
import kr.daeho.AssetAssistant.assets.vo.ExpenseVo;
import kr.daeho.AssetAssistant.assets.vo.AssetsDetailVo;

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

    // 사용자 아이디 (식별자)
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

    // 자산 정보 생성일
    private LocalDateTime createdAt;

    // 자산 정보 수정일
    private LocalDateTime updatedAt;
}
