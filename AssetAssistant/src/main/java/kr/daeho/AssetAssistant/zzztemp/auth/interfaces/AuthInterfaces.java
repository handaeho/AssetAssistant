package kr.daeho.AssetAssistant.zzztemp.auth.interfaces;

import kr.daeho.AssetAssistant.auth.dto.LoginRequestDto;

/**
 * 로그인 인증 및 토큰 관리 인터페이스
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
     * 로그인
     * 
     * @param LoginRequestDto 로그인 요청 정보
     * @return 생성된 JWT 토큰
     * @throws ApplicationException.AuthenticationFailedException 인증 실패 시
     */
    String login(LoginRequestDto loginRequestDto);

    /**
     * 토큰 유효성 검증
     * 
     * @param token 검증할 토큰
     * @return 토큰 유효성 검증 결과
     */
    boolean validateToken(String token);

    /**
     * 토큰에서 사용자 아이디 추출
     * 
     * @param token 토큰
     * @return 사용자 아이디
     * @throws ApplicationException.InvalidTokenException 토큰이 유효하지 않을 때
     */
    String getUserIdFromToken(String token);

    // TODO: 토큰 갱신? 로그아웃? 토큰 삭제?
}
