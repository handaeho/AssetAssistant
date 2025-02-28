package kr.daeho.AssetAssistant.assets.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.daeho.AssetAssistant.assets.dto.AssetsDto;
import kr.daeho.AssetAssistant.assets.interfaces.AssetsInterfaces;
import kr.daeho.AssetAssistant.common.controller.BaseController;
import kr.daeho.AssetAssistant.common.dto.ApiResponse;

/**
 * 자산 컨트롤러
 * 
 * 자산 정보를 조회, 등록, 수정, 삭제하는 기능을 제공
 * 
 * 애플리케이션의 진입점 역할로, 클라이언트의 요청을 받아 서비스 계층으로 전달하고, 결과를 클라이언트에 반환
 * 
 * 컨트롤러는 클라이언트의 요청을 받아 서비스 계층으로 전달하고, 결과를 클라이언트에 반환
 * 
 * ResponseEntity: HTTP 상태 코드(예: 200 OK, 204 No Content, 404 Not Found 등)를 함께 반환
 * 
 * 생성자를 통한 의존성 주입 -> 의존성 역전 원칙 (불변성 보장, 필수 의존성 보장, 테스트 용이)
 * 
 * 고수준 모듈(컨트롤러)이 저수준 모듈(서비스)에 직접 의존하지 않음 (인터페이스(계약)에 의존)
 * 
 * @RestController: 컨트롤러 클래스임을 명시
 * @RequestMapping: 자산 관련 요청 URL과 매핑하기 위한 기본 프리픽스(~/assets/~)
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션 (@Autowired 대신 사용)
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Slf4j
public class AssetsController extends BaseController {
    // 인터페이스 선언 (final로 선언해 불변성 보장)
    // 컨트롤러는 서비스(실제)가 아닌 인터페이스(계약)에 의존하여 의존성 역전 및 느슨한 결합 확보
    // @RequiredArgsConstructor로 생성자 자동 생성 및 의존성 주입
    private final AssetsInterfaces assetsInterfaces;

    /**
     * 사용자의 자산 정보 조회
     * 
     * @param userId 사용자 ID
     * @return 자산 정보
     */
    @GetMapping("/info/{userId}")
    public ResponseEntity<ApiResponse<AssetsDto>> getAssetsInfo(@PathVariable String userId) {
        log.info("자산 정보 조회 요청 처리: {}", userId);

        // userId를 통해 자산 정보 검색 및 반환
        AssetsDto assetsDto = assetsInterfaces.getAssetsInfo(userId);

        log.info("자산 정보 조회 완료: {}", userId);

        return success(assetsDto, "자산 정보 조회 성공");
    }

    /**
     * 사용자의 자산 정보 등록
     * 
     * @param userId    사용자 ID
     * @param assetsDto 자산 정보 DTO
     * @return 자산 정보
     */
    @PostMapping("/create/{userId}")
    public ResponseEntity<ApiResponse<AssetsDto>> createAssets(@PathVariable String userId,
            @RequestBody AssetsDto assetsDto) {
        log.info("자산 정보 등록 요청 처리: {}", userId);

        // userId를 통해 자산 정보 등록 및 반환
        AssetsDto createdAssetsDto = assetsInterfaces.createAssets(userId, assetsDto);

        log.info("자산 정보 등록 완료: {}", userId);

        return success(createdAssetsDto, "자산 정보 등록 성공");
    }

    /**
     * 사용자의 자산 정보 수정
     * 
     * @param userId    사용자 ID
     * @param assetsDto 자산 정보 DTO
     * @return 자산 정보
     */
    @PutMapping("/update/{userId}")
    public ResponseEntity<ApiResponse<AssetsDto>> updateAssets(@PathVariable String userId,
            @RequestBody AssetsDto assetsDto) {
        log.info("자산 정보 수정 요청 처리: {}", userId);

        // userId를 통해 자산 정보 수정 및 반환
        AssetsDto updatedAssetsDto = assetsInterfaces.updateAssets(userId, assetsDto);

        log.info("자산 정보 수정 완료: {}", userId);

        return success(updatedAssetsDto, "자산 정보 수정 성공");
    }

    /**
     * 사용자의 자산 정보 삭제
     * 
     * @param userId 사용자 ID
     * @return 자산 정보
     */
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteAssets(@PathVariable String userId) {
        log.info("자산 정보 삭제 요청 처리: {}", userId);

        // userId를 통해 자산 정보 삭제
        assetsInterfaces.deleteAssets(userId);

        log.info("자산 정보 삭제 완료: {}", userId);

        // 삭제 성공 시 204(콘텐츠 없음) 응답
        return noContent();
    }
}
