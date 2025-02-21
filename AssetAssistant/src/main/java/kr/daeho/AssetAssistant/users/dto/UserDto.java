package kr.daeho.AssetAssistant.users.dto;

import kr.daeho.AssetAssistant.users.entity.UserEntity;
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
    private String userId; // 사용자 아이디
    private String userName; // 사용자 이름
    private String userPassword; // 사용자 비밀번호
    private int userAge; // 사용자 나이
    private String userJob; // 사용자 직업

    // UserDto를 UserEntity로 변환
    // DTO를 Entity로 변환할 때는 특정 DTO 인스턴스의 현재 상태(this)가 필요하므로 인스턴스 메서드로 구현
    public UserEntity toUserEntity() {
        return UserEntity.builder()
                .userId(userId) // 사용자 아이디
                .userName(userName) // 사용자 이름
                .userPassword(userPassword) // 사용자 비밀번호
                .userAge(userAge) // 사용자 나이
                .userJob(userJob) // 사용자 직업
                .build();
    }

    // UserEntity를 UserDto로 변환
    // Entity를 DTO로 변환할 때는 특정 DTO 인스턴스의 상태가 필요하지 않으므로 static 메서드로 구현
    public static UserDto fromUserEntity(UserEntity userEntity) {
        return UserDto.builder()
                .userId(userEntity.getUserId())
                .userName(userEntity.getUserName())
                .userPassword(userEntity.getUserPassword())
                .userAge(userEntity.getUserAge())
                .userJob(userEntity.getUserJob())
                .build();
    }
}
