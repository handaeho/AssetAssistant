package kr.daeho.AssetAssistant.assets.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 수입 유형을 정의하는 열거형 클래스
 * 
 * 고정 수입과 변동 수입을 명확하게 구분하여 타입 안전성 제공
 */
@Getter
@AllArgsConstructor
public enum IncomeType {
    FIXED("고정", "매월 일정하게 발생하는 수입"),
    VARIABLE("변동", "불규칙적으로 발생하는 수입");

    // 수입 유형 코드
    private final String code;

    // 수입 유형 설명
    private final String description;

    /**
     * 코드값으로 IncomeType을 찾아 반환
     * 
     * @param code 찾을 코드값
     * @return 찾은 IncomeType, 없는 경우 VARIABLE 반환
     */
    public static IncomeType fromCode(String code) {
        if (code == null) {
            return VARIABLE;
        }

        for (IncomeType type : IncomeType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }

        return VARIABLE;
    }

    @Override
    public String toString() {
        return code;
    }
}