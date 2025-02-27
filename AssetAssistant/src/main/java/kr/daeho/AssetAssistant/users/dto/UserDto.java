package kr.daeho.AssetAssistant.users.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보 DTO
 * 
 * DTO: 클라이언트와 서비스 간 데이터 전송을 위한 객체
 * 
 * Entity와 비슷하지만, 클라이언트 요청 및 응답에 필요한 데이터만 포함.
 * 
 * DTO는 컨트롤러와 서비스 계층에서 엔티티의 변경을 직접적으로 노출하지 않고, 필요한 데이터만 클라이언트에게 전송.
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
public class UserDto {
    // DB에서 사용되는 고유식별자(ID)
    private Long id;

    // 사용자 아이디 (식별자)
    @NotBlank(message = "사용자 아이디 - 필수 항목")
    @Size(min = 4, max = 16, message = "사용자 아이디 - 4자 이상 16자 이하")
    private String userId;

    // 사용자 비밀번호
    @NotBlank(message = "사용자 비밀번호 - 필수 항목")
    @Size(min = 8, max = 16, message = "사용자 비밀번호 - 8자 이상 16자 이하")
    private String userPassword;

    // 사용자 이름
    @NotBlank(message = "사용자 이름 - 필수 항목")
    @Size(min = 2, max = 10, message = "사용자 이름 - 2자 이상 10자 이하")
    private String userName;

    // 사용자 나이
    private int userAge;

    // 사용자 직업
    private String userJob;

    // 사용자 정보 수정일
    private LocalDateTime userUpdatedAt;
}
