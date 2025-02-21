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
    // DB에서 사용되는 고유식별자(ID)
    private Long id;

    // 사용자 아이디 (식별자)
    private String userId;

    // 사용자 이름
    private String userName;

    // 사용자 비밀번호
    private String userPassword;

    // 사용자 나이
    private int userAge;

    // 사용자 직업
    private String userJob;

    /**
     * UserDto를 UserEntity로 변환하는 메소드
     * 
     * 신규 생성이라면, DB에서 필요한 모든 필드 값을 빌드해야 하고 (기본 값, 자동 생성 값 등 포함)
     * 업데이트라면, 업데이트 할 필드만 빌드하면 나머지는 DB에 있는 기존 값을 유지하게 가능
     * DTO를 Entity로 변환할 때는 특정 DTO 인스턴스의 현재 상태(this)가 필요하므로 인스턴스 메서드로 구현
     * 
     * @return 변환된 UserEntity 객체
     */
    public UserEntity toUserEntity() {
        return UserEntity.builder()
                .id(id) // DB에서 사용되는 고유식별자(ID)
                .userId(userId) // 사용자 아이디
                .userName(userName) // 사용자 이름
                .userPassword(userPassword) // 사용자 비밀번호
                .userAge(userAge) // 사용자 나이
                .userJob(userJob) // 사용자 직업
                .build();
    }

    /**
     * UserEntity를 UserDto로 변환하는 메소드
     * 
     * DB에서 클라이언트에게 전달할 데이터만 빌드
     * Entity를 DTO로 변환할 때는 특정 DTO 인스턴스의 상태가 필요하지 않으므로 static 메서드로 구현
     * 
     * @param entity 변환할 엔티티
     * @return 변환된 DTO
     */
    public static UserDto fromUserEntity(UserEntity userEntity) {
        return UserDto.builder()
                .userId(userEntity.getUserId()) // 사용자 아이디
                .userName(userEntity.getUserName()) // 사용자 이름
                .userPassword(userEntity.getUserPassword()) // 사용자 비밀번호
                .userAge(userEntity.getUserAge()) // 사용자 나이
                .userJob(userEntity.getUserJob()) // 사용자 직업
                .build();
    }
}
