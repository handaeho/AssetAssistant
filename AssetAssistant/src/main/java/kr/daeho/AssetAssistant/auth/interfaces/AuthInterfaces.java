package kr.daeho.AssetAssistant.auth.interfaces;

import java.util.Map;
import kr.daeho.AssetAssistant.auth.dto.LoginRequestDto;

/**
 * 사용자 인증 인터페이스
 * 
 * 클래스에서 가져야하는 메소드의 이름, 파라미터, 리턴값 등을 정의
 * 
 * 클라이언트 <-> 컨트롤러 <-> 인터페이스 <-> 서비스 <-> 레포지토리 <-> DB
 * 
 * 추상화: 구현 세부사항은 감추고 어떤 기능을 제공하는 지에 대해만 정의
 * 다형성: 서로 다른 클래스가 같은 인터페이스를 상속받아 동일한 동작을 구현 가능
 * 느슨한 결합: 실제로 기능을 구현하는 클래스에 의존하지 않고, 인터페이스에 의존해 유연성 확보
 * 역할 분리: 클라이언트는 컨트롤러 및 인터페이스와 소통하고, 내부 서비스에 접근하지 않아도 됨
 */
public interface AuthInterfaces {
    /**
     * 사용자 로그인 처리
     * 
     * @param loginRequestDto 로그인 요청 정보
     * @return Map<String, Object> 로그인 성공 시 토큰 정보 (사용자 ID, 액세스 토큰, 리프레시 토큰)
     */
    Map<String, Object> login(LoginRequestDto loginRequestDto);

    /**
     * 토큰 검증
     * 
     * @param accessToken 액세스 토큰
     * @return 사용자 ID
     */
    String validateToken(String accessToken);

    /**
     * 토큰 갱신 - 리프레시 토큰으로 새 액세스 토큰 발급
     * 
     * @param refreshToken 리프레시 토큰
     * @return Map<String, Object> 갱신된 새 토큰 정보 (사용자 ID, 액세스 토큰, 리프레시 토큰)
     */
    Map<String, Object> refreshToken(String refreshToken);

    /**
     * 로그아웃 처리 - 모든 디바이스
     * 
     * @param userId 사용자 ID
     */
    void logout(String userId);

    /**
     * 로그아웃 처리 - 특정 디바이스
     * 
     * @param userId   사용자 ID
     * @param deviceId 디바이스 ID
     */
    void logout(String userId, String deviceId);

    // default 메소드
    // 인터페이스에서 작성한 기능은 반드시 상속받는 구현 메소드에서 작성되어야 함함
    // 나중에 추가된 기능이나, 구현할 수도 있고 안할 수도 있는 기능이면 default 메소드를 사용
    // 단, 같은 default 메소드를 정의한 여러개의 인터페이스를 한 클래스가 다중 상속하면 안됨 (다중 상속 충돌)

    // static 메소드
    // 인터페이스 내부에서 사용하는 기능이 있는 메소드
    // static 메소드는 특정 객체에 속하지 않기 때문에, 인터페이스를 구현하는 클래스의 API의 한 부분이 아님
    // 따라서 메소드명 앞에 인터페이스 이름을 넣어서 호출 ("인터페이스명"."static 메소드명()")
}
