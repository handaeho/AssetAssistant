package kr.daeho.AssetAssistant.assets.vo;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * 자산 상세 정보 Value Object(VO)
 * 
 * VO는 도메인에서 값 자체가 고유성을 갖는 불변객체
 * 
 * 수정할 경우, 새로운 VO 객체를 생성해서 수정 필요. 같은 값이면, 같은 객체로 간주
 * 
 * @Embeddable: JPA에서 임베디드 타입으로 사용할 수 있게 해주는 어노테이션
 *              엔티티의 특정 필드나 컬렉션에 포함되어 저장
 * @Getter: 모든 필드의 Getter 메서드를 자동으로 생성
 *          setter는 의도적으로 제외하여 불변성 유지
 * @Builder: 빌더 패턴을 사용하여 객체 생성을 용이하게 함
 *           생성 후 setter 사용을 배제하여 불변성 유지
 * @NoArgsConstructor: 기본 생성자를 자동으로 생성 (JPA에서 필수)
 * @AllArgsConstructor: 모든 필드를 매개변수로 하는 생성자를 자동으로 생성
 * @EqualsAndHashCode: equals()와 hashCode() 메서드를 자동으로 생성
 *                     VO는 값이 같으면 같은 객체로 취급하므로 필수
 */
@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AssetsDetailVo {
    /**
     * 자산 이름 (예: 삼성전자주식, 국민은행적금 등)
     * 사용자가 구분할 수 있는 구체적인 자산명
     */
    private String assetsName;

    /**
     * 자산 타입 (예: 주식, 예금, 적금, 부동산 등)
     * 자산의 종류를 구분하는 카테고리
     */
    private String assetsType;

    /**
     * 자산 금액 (원 단위)
     * 해당 자산의 현재 평가 금액
     */
    private int assetsPrice;

    /**
     * 자산 정보를 문자열로 반환
     * 
     * @return 자산 정보 문자열
     */
    @Override
    public String toString() {
        return String.format("AssetsDetail(name=%s, type=%s, price=%d)",
                assetsName, assetsType, assetsPrice);
    }
}
