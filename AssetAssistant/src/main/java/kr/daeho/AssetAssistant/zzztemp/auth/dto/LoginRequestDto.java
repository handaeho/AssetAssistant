package kr.daeho.AssetAssistant.zzztemp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 로그인 요청 정보를 담는 DTO 클래스
 * 
 * 클라이언트로부터 받은 로그인 정보를 검증하고 전달하는 역할
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
public class LoginRequestDto {
    /**
     * 사용자 아이디
     * 공백 불가
     */
    @NotBlank(message = "아이디는 필수 입력값입니다")
    private String userId;

    /**
     * 사용자 비밀번호
     * 공백 불가
     */
    @NotBlank(message = "비밀번호는 필수 입력값입니다")
    private String password;
}
