package kr.daeho.AssetAssistant.assets.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.daeho.AssetAssistant.assets.dto.AssetsDto;
import kr.daeho.AssetAssistant.assets.interfaces.AssetsInterfaces;

/**
 * 자산 컨트롤러
 * 
 * 자산 정보를 조회, 등록, 수정, 삭제하는 기능을 제공
 * 
 * 애플리케이션의 진입점 역할로, 클라이언트의 요청을 받아 서비스 계층으로 전달하고, 결과를 클라이언트에 반환
 * 
 * 컨트롤러는 클라이언트의 요청을 받아 서비스 계층으로 전달하고, 결과를 클라이언트에 반환
 * 
 * @RestController: 컨트롤러 클래스임을 명시
 * @RequestMapping: 자산 관련 요청 URL과 매핑하기 위한 기본 프리픽스(~/assets/~)
 */
@RestController
@RequestMapping("/assets")
public class AssetsController {
    // 인터페이스 선언 (final로 선언해 불변성 보장)
    // 컨트롤러는 서비스(실제)가 아닌 인터페이스(계약)에 의존하여 의존성 역전 및 느슨한 결합 확보
    private final AssetsInterfaces assetsInterfaces;

    // 생성자를 통한 의존성 주입 -> 의존성 역전 원칙 (불변성 보장, 필수 의존성 보장, 테스트 용이)
    // 고수준 모듈(컨트롤러)이 저수준 모듈(서비스)에 직접 의존하지 않음 (인터페이스(계약)에 의존)
    @Autowired
    public AssetsController(AssetsInterfaces assetsInterfaces) {
        this.assetsInterfaces = assetsInterfaces;
    }

    // 자산 정보 조회
    @GetMapping("/info")
    public AssetsDto getAssetsInfo(@RequestParam String userId) {
        // userId를 통해 자산 정보 검색 및 반환
        AssetsDto assetsDto = assetsInterfaces.getAssetsInfo(userId);
        return assetsDto;
    }

    // 자산 정보 등록
    // 자산 정보 수정
    // 자산 정보 삭제
}
