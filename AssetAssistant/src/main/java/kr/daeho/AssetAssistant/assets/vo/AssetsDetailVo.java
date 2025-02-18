package kr.daeho.AssetAssistant.assets.vo;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자산 정보 Value Object(VO)
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
public class AssetsDetailVo {
    // 자산 이름 (XX적금, XX주식 등)
    private String assetsName;

    // 자산 타입 (예금, 주식, 채권 등)
    private String assetsType;

    // 자산 금액
    private int assetsPrice;
}
