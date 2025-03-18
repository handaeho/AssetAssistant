package kr.daeho.AssetAssistant.assets.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 자산 유형을 정의하는 열거형 클래스
 * 
 * 애플리케이션에서 사용되는 자산 유형들을 명확하게 정의하고,
 * 문자열 비교 대신 타입 안전한 열거형을 사용하여 오류 가능성을 줄임
 */
@Getter
@AllArgsConstructor
public enum AssetType {
    STOCK("주식", "주식 자산"),
    SAVINGS_ACCOUNT("예금", "예금 자산"),
    INSTALLMENT_SAVINGS("적금", "적금 자산"),
    REAL_ESTATE("부동산", "부동산 자산"),
    CRYPTOCURRENCY("암호화폐", "암호화폐 자산"),
    BOND("채권", "채권 자산"),
    CASH("현금", "현금 자산"),
    GOLD("금", "금 자산"),
    OTHER("기타", "기타 자산");

    // 자산 유형의 코드값 (UI 표시 및 API 요청/응답에 사용)
    private final String code;

    // 자산 유형의 설명
    private final String description;

    /**
     * 코드값으로 AssetType을 찾아 반환
     * 
     * @param code 찾을 코드값
     * @return 찾은 AssetType, 없는 경우 OTHER 반환
     */
    public static AssetType fromCode(String code) {
        if (code == null) {
            return OTHER;
        }

        for (AssetType type : AssetType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }

        return OTHER;
    }

    @Override
    public String toString() {
        return code;
    }
}