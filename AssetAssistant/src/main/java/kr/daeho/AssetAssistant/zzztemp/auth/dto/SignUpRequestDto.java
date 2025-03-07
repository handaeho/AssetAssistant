package kr.daeho.AssetAssistant.zzztemp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 회원가입 요청 정보를 담는 DTO 클래스
 * 
 * 클라이언트로부터 받은 회원가입 정보를 검증하고 전달하는 역할
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
public class SignUpRequestDto {
    /**
     * 사용자 아이디
     * 공백 불가, 최소 4자 최대 20자
     */
    @NotBlank(message = "아이디는 필수 입력값입니다")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해주세요")
    private String userId;

    /**
     * 사용자 비밀번호
     * 공백 불가, 최소 8자 최대 20자
     */
    @NotBlank(message = "비밀번호는 필수 입력값입니다")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요")
    private String password;

    /**
     * 사용자 이름
     * 공백 불가, 최소 2자 최대 10자
     */
    @NotBlank(message = "이름은 필수 입력값입니다")
    @Size(min = 2, max = 10, message = "이름은 2자 이상 10자 이하로 입력해주세요")
    private String userName;
}
