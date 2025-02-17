package kr.daeho.AssetAssistant.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.daeho.AssetAssistant.user.dto.UserDto;
import kr.daeho.AssetAssistant.user.interfaces.UserInterfaces;

/**
 * 사용자 컨트롤러
 * 
 * 사용자 정보를 조회, 등록, 수정, 삭제하는 기능을 제공
 * 
 * 애플리케이션의 진입점 역할로, 클라이언트의 요청을 받아 서비스 계층으로 전달하고, 결과를 클라이언트에 반환
 * 
 * 컨트롤러는 클라이언트의 요청을 받아 서비스 계층으로 전달하고, 결과를 클라이언트에 반환
 * 
 * @RestController: 컨트롤러 클래스임을 명시
 * @RequestMapping: 사용자 관련 요청 URL과 매핑하기 위한 기본 프리픽스(~/user/~)
 */
@RestController
@RequestMapping("/user")
public class UserController {
    // 인터페이스 선언 (final로 선언해 불변성 보장)
    // 컨트롤러는 서비스(실제)가 아닌 인터페이스(계약)에 의존하여 의존성 역전 및 느슨한 결합 확보
    private final UserInterfaces userInterfaces;

    @Autowired
    public UserController(UserInterfaces userInterfaces) {
        this.userInterfaces = userInterfaces;
    }

    // 사용자 정보 조회
    @GetMapping("/info")
    public UserDto getUserInfo(@RequestParam String userId) {
        // userId를 통해 사용자 정보 검색 및 반환
        UserDto userDto = userInterfaces.getUserInfo(userId);
        return userDto;
    }
    
}