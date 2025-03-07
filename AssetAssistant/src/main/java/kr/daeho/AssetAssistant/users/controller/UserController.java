package kr.daeho.AssetAssistant.users.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kr.daeho.AssetAssistant.users.dto.UserDto;
import kr.daeho.AssetAssistant.users.dto.SignupRequestDto;
import kr.daeho.AssetAssistant.users.interfaces.UserInterfaces;
import kr.daeho.AssetAssistant.users.interfaces.UserSignupInterfaces;
import kr.daeho.AssetAssistant.common.controller.BaseController;
import kr.daeho.AssetAssistant.common.dto.ApiResponse;

/**
 * 사용자 컨트롤러 -> 사용자 정보를 조회, 등록, 수정, 삭제 기능 제공
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
 * @RequestMapping: 사용자 관련 요청 URL과 매핑하기 위한 기본 프리픽스(~/user/~)
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController extends BaseController {
    // 인터페이스 선언 (final로 선언해 불변성 보장)
    // 컨트롤러는 서비스(실제)가 아닌 인터페이스(계약)에 의존하여 의존성 역전 및 느슨한 결합 확보
    // @RequiredArgsConstructor로 생성자 자동 생성 및 의존성 주입
    private final UserInterfaces userInterfaces;
    private final UserSignupInterfaces userSignupInterfaces;

    /**
     * 회원 가입 처리
     * 
     * @param signUpRequestDto 회원 가입 요청 정보
     * @return 회원 가입 성공 시 200 OK 응답
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(
            @Valid @RequestBody SignupRequestDto signUpRequestDto) {
        log.info("회원가입 요청: {}", signUpRequestDto.getUserId());

        // 회원가입 처리 (예외 발생 시 GlobalExceptionHandler로 전파)
        userSignupInterfaces.signup(signUpRequestDto);

        return success(null, "회원가입이 완료되었습니다");
    }

    /**
     * 사용자 정보 조회
     * 
     * @param userId 사용자 아이디
     * @return 사용자 정보 DTO
     */
    @GetMapping("/info/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUserInfo(@PathVariable String userId) {
        // userId를 통해 사용자 정보 검색 및 반환
        log.info("사용자 정보 조회: {}", userId);

        // 사용자 조회 (사용자 없으면 UserNotFoundException 발생, GlobalExceptionHandler로 전파)
        UserDto userDto = userInterfaces.getUserInfo(userId);

        return success(userDto, "사용자 정보 조회가 완료되었습니다");
    }

    /**
     * 사용자 정보 수정
     * 
     * @param userId  사용자 아이디
     * @param userDto 수정할 사용자 정보
     * @return 수정된 사용자 정보 DTO
     */
    @PutMapping("/update/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable String userId,
            @Valid @RequestBody UserDto userDto) {
        // userId를 통해 사용자 정보 수정 및 반환
        log.info("사용자 정보 수정: {}", userId);

        // 사용자 정보 수정 (예외 발생 시 GlobalExceptionHandler로 전파)
        UserDto updatedUserDto = userInterfaces.updateUser(userId, userDto);

        return success(updatedUserDto, "사용자 정보 수정이 완료되었습니다");
    }

    /**
     * 사용자 삭제
     * 
     * @param userId 사용자 아이디
     * @return 삭제 성공 시 204(콘텐츠 없음) 응답
     */
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteUser(@RequestParam String userId) {
        // userId를 통해 사용자 정보 삭제
        log.info("사용자 삭제 요청: {}", userId);

        // 사용자 삭제 (예외 발생 시 GlobalExceptionHandler로 전파)
        userInterfaces.deleteUser(userId);

        return noContent();
    }

    /**
     * 비밀번호 변경
     * 
     * @param userId          사용자 아이디
     * @param currentPassword 현재 비밀번호
     * @param newPassword     새로운 비밀번호
     * @return 비밀번호 변경 성공 시 200 OK 응답
     */
    @PutMapping("/change-password/{userId}")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable String userId,
            @RequestParam String currentPassword, @RequestParam String newPassword) {
        // userId를 통해 사용자 정보 삭제
        log.info("비밀번호 변경 요청: {}", userId);

        // 비밀번호 변경 (예외 발생 시 GlobalExceptionHandler로 전파)
        userInterfaces.changePassword(userId, currentPassword, newPassword);

        return success(null, "비밀번호 변경이 완료되었습니다");
    }
}