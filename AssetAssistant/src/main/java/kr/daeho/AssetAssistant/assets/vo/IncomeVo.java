package kr.daeho.AssetAssistant.assets.vo;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * 수입 정보 Value Object(VO)
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
 * @EqualsAndHashCode: equals()와 hashCode() 메서드를 자동으로 생성
 *                     VO는 값이 같으면 같은 객체로 취급하므로 필수 (값 객체의 동등성 비교를 위해 적용)
 */
@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class IncomeVo {
    // 수입 이름 (XX급여, XX배당금, XX이자 등)
    private String incomeName;

    // 수입 금액 (원 단위)
    private int incomeAmount;

    /**
     * 고정 수입 또는 변동 수입 타입
     * 
     * @Enumerated: 열거형 타입을 데이터베이스 컬럼에 매핑
     *              EnumType.STRING: 열거형 값을 문자열로 저장 (FIXED, VARIABLE)
     */
    @Enumerated(EnumType.STRING)
    private IncomeType incomeType;

    /**
     * String 타입의 수입 유형을 받는 정적 팩토리 메서드
     * 
     * @param incomeName     수입 이름
     * @param incomeAmount   수입 금액
     * @param incomeTypeCode 수입 유형 코드
     * @return 생성된 IncomeVo 객체
     */
    public static IncomeVo of(String incomeName, int incomeAmount, String incomeTypeCode) {
        return IncomeVo.builder()
                .incomeName(incomeName)
                .incomeAmount(incomeAmount)
                .incomeType(IncomeType.fromCode(incomeTypeCode))
                .build();
    }

    /**
     * 수입 정보를 문자열로 반환
     * 
     * @return 수입 정보 문자열
     */
    @Override
    public String toString() {
        return String.format("Income(name=%s, amount=%d, type=%s)",
                incomeName, incomeAmount, incomeType);
    }
}
