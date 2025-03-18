package kr.daeho.AssetAssistant.assets.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 지출 유형을 정의하는 열거형 클래스
 * 
 * 고정 지출과 변동 지출을 명확하게 구분하여 타입 안전성 제공
 */
@Getter
@AllArgsConstructor
public enum ExpenseType {
    FIXED("고정", "매월 일정하게 발생하는 지출"),
    VARIABLE("변동", "불규칙적으로 발생하는 지출");

    // 지출 유형 코드
    private final String code;

    // 지출 유형 설명
    private final String description;

    /**
     * 코드값으로 ExpenseType을 찾아 반환
     * 
     * @param code 찾을 코드값
     * @return 찾은 ExpenseType, 없는 경우 VARIABLE 반환
     */
    public static ExpenseType fromCode(String code) {
        if (code == null) {
            return VARIABLE;
        }

        for (ExpenseType type : ExpenseType.values()) {
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