package kr.daeho.AssetAssistant.assets.vo;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지출(고정 및 변동) 정보 Value Object(VO)
 * 
 * VO는 도메인에서 값 자체가 고유성을 갖는 불변객체
 * 
 * 수정할 경우, 새로운 VO 객체를 생성해서 수정 필요. 같은 값이면, 같은 객체로 간주
 * 
 * @Embeddable: JPA에서 임베디드 타입으로 사용할 수 있게 해주는 어노테이션
 * @Getter: 모든 필드의 Getter 메서드를 자동으로 생성. setter는 의도적으로 제외하여 불변성 유지
 * @Builder: 빌더 패턴 사용. 객체 생성 후 setter는 제외하여 불변성 유지
 * @NoArgsConstructor: 기본 생성자를 자동으로 생성. JPA Entity에 필수
 * @AllArgsConstructor: 모든 필드를 매개변수로 하는 생성자를 자동으로 생성
 */
@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseVo {
    // 고정 지출 이름 (월세, 공과금 등)
    private String fixedExpenseName;

    // 고정 지출 타입 (주거비, 공과금, 보험료 등)
    private String fixedExpenseType;

    // 고정 지출 금액
    private int fixedExpenseAmount;

    // 변동 지출 이름 (여행, 취미 등)
    private String variableExpenseName;

    // 변동 지출 타입 (여가, 문화생활 등)
    private String variableExpenseType;

    // 변동 지출 금액
    private int variableExpenseAmount;
}
