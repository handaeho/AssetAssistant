package kr.daeho.AssetAssistant.common.controller;

import kr.daeho.AssetAssistant.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 모든 컨트롤러의 기본 클래스. 공통 응답 처리 로직 제공
 * 
 * abstract: 추상 클래스로 선언. 컨트롤러 클래스에서 상속받아 사용
 * 
 * 추상 클래스: 비슷한 필드와 메서드를 공통적으로 추출해 만들어진 클래스
 * 추상클래스는 아직 메서드와 내용이 추상적이기 때문에 객체를 생성할 수 없음.
 * 추상클래스를 상속받은 클래스에서 추상클래스의 메서드를 구현해야 객체를 생성할 수 있음.
 */
public abstract class BaseController {
    /**
     * 성공 응답 생성 (200 OK)
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 성공 응답 생성 (메시지 포함, 200 OK)
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * 성공 응답 생성 (상태 코드 지정)
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(HttpStatus status, T data, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.success(status.value(), message, data));
    }

    /**
     * 생성 성공 응답 (201 Created)
     */
    protected <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return success(HttpStatus.CREATED, data, "리소스가 생성되었습니다");
    }

    /**
     * 삭제 성공 응답 (내용 없음 204 No Content)
     */
    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }
}
