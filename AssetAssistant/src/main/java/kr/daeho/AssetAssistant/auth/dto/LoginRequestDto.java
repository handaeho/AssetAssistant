package kr.daeho.AssetAssistant.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 로그인 정보 DTO -> 로그인 할 때 입력되는 아이디와 비밀번호 정보를 담는 객체
 * 
 * DTO: 클라이언트와 서비스 간 데이터 전송을 위한 객체
 * 
 * Entity와 비슷하지만, 클라이언트 요청 및 응답에 필요한 데이터만 포함.
 * 
 * DTO는 컨트롤러와 서비스 계층에서 엔티티의 변경을 직접적으로 노출하지 않고, 필요한 데이터만 클라이언트에게 전송.
 * 
 * NOTE: auth 패키지는 주로 로그인 서비스 로직에 집중되고,
 * 사용자 정보는 이미 users 패키지에 있는 UserEntity와 UserRepository를 활용함
 * 또한, JWT는 상태를 서버에 저장하지 않는(stateless) 방식이기 때문에 Entity와 Repository를 활용하지 않음
 * 
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
    // 사용자 아이디 (식별자)
    @NotBlank(message = "사용자 아이디는 필수 항목입니다")
    @Size(min = 4, max = 16, message = "사용자 아이디는 4자 이상 16자 이하여야 합니다")
    private String userId;

    // 사용자 비밀번호
    @NotBlank(message = "비밀번호는 필수 항목입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,}$", message = "비밀번호는 최소 하나의 문자, 숫자, 특수문자를 포함해야 합니다")
    private String password;

    // 디바이스 식별자 (선택 사항) - 여러 기기에서의 로그인 지원을 위해 추가
    private String deviceId;
}
