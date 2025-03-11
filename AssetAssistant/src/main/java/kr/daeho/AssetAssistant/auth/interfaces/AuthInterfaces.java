package kr.daeho.AssetAssistant.auth.interfaces;

import kr.daeho.AssetAssistant.auth.dto.TokenResponseDto;
import kr.daeho.AssetAssistant.auth.dto.LoginRequestDto;

/**
 * 사용자 로그인 인터페이스
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
     * 사용자 로그인
     * 
     * @param loginRequestDto 로그인 요청 정보
     * @throws ApplicationExceptions 아이디 중복 등 예외 발생 시
     * @return TokenResponseDto (액세스 토큰과 리프레시 토큰, 만료시간, 타입)
     */
    TokenResponseDto login(LoginRequestDto loginRequestDto);

    // TODO: 토큰 갱신 시, 액세스 토큰과 리프레시 토큰 모두 재발급하게 변경

    /**
     * 리프레시 토큰을 받아, 새 액세스 토큰을 발급 (리프레시 토큰은 기존 것을 유지)
     * 
     * @param refreshToken 리프레시 토큰
     * @throws ApplicationExceptions 아이디 중복 등 예외 발생 시
     * @return TokenResponseDto (새로운 액세스 토큰, 기존 리프레시 토큰, 만료시간, 타입)
     */
    TokenResponseDto refreshToken(String refreshToken);

    /**
     * 로그아웃 처리
     * 
     * @param token 액세스 토큰
     */
    void logout(String token);

    // default 메소드
    // 인터페이스에서 작성한 기능은 반드시 상속받는 구현 메소드에서 작성되어야 함함
    // 나중에 추가된 기능이나, 구현할 수도 있고 안할 수도 있는 기능이면 default 메소드를 사용
    // 단, 같은 default 메소드를 정의한 여러개의 인터페이스를 한 클래스가 다중 상속하면 안됨 (다중 상속 충돌)

    // static 메소드
    // 인터페이스 내부에서 사용하는 기능이 있는 메소드
    // static 메소드는 특정 객체에 속하지 않기 때문에, 인터페이스를 구현하는 클래스의 API의 한 부분이 아님
    // 따라서 메소드명 앞에 인터페이스 이름을 넣어서 호출 ("인터페이스명"."static 메소드명()")
}
