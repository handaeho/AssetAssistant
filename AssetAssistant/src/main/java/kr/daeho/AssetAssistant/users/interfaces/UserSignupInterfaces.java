package kr.daeho.AssetAssistant.users.interfaces;

import kr.daeho.AssetAssistant.users.dto.UserDto;
import kr.daeho.AssetAssistant.users.dto.SignupRequestDto;

public interface UserSignupInterfaces {
    /**
     * 사용자 생성 (회원가입)
     * 
     * @param signUpRequestDto 회원가입 요청 정보
     * @throws ApplicationExceptions 아이디 중복 등 예외 발생 시
     */
    UserDto signup(SignupRequestDto signupRequestDto);

    // default 메소드
    // 인터페이스에서 작성한 기능은 반드시 상속받는 구현 메소드에서 작성되어야 함함
    // 나중에 추가된 기능이나, 구현할 수도 있고 안할 수도 있는 기능이면 default 메소드를 사용
    // 단, 같은 default 메소드를 정의한 여러개의 인터페이스를 한 클래스가 다중 상속하면 안됨 (다중 상속 충돌)

    // static 메소드
    // 인터페이스 내부에서 사용하는 기능이 있는 메소드
    // static 메소드는 특정 객체에 속하지 않기 때문에, 인터페이스를 구현하는 클래스의 API의 한 부분이 아님
    // 따라서 메소드명 앞에 인터페이스 이름을 넣어서 호출 ("인터페이스명"."static 메소드명()")
}
