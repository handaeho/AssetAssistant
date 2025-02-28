package kr.daeho.AssetAssistant.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 표준 응답 클래스. 모든 API 응답을 일관된 형식으로 제공
 * 
 * @Getter: 모든 필드의 Getter 메서드를 자동으로 생성. setter는 의도적으로 제외하여 불변성 유지
 * @Builder: 빌더 패턴 사용. 객체 생성 후 setter는 제외하여 불변성 유지
 * @NoArgsConstructor: 기본 생성자를 자동으로 생성. JPA Entity에 필수
 * @AllArgsConstructor: 모든 필드를 매개변수로 하는 생성자를 자동으로 생성
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success; // 성공 여부
    private int status; // 상태 코드
    private String message; // 메시지
    private String errorCode; // 에러 코드
    private T data; // 데이터

    /**
     * 성공 응답 생성 (데이터만)
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, "요청이 성공적으로 처리되었습니다", null, data);
    }

    /**
     * 성공 응답 생성 (데이터와 메시지)
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, 200, message, null, data);
    }

    /**
     * 성공 응답 생성 (상태 코드 지정)
     */
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(true, status, message, null, data);
    }

    /**
     * 오류 응답 생성 (기본)
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(false, status, message, null, null);
    }

    /**
     * 오류 응답 생성 (에러 코드 포함)
     */
    public static <T> ApiResponse<T> error(int status, String message, String errorCode) {
        return new ApiResponse<>(false, status, message, errorCode, null);
    }

    /**
     * 오류 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> error(int status, String message, String errorCode, T data) {
        return new ApiResponse<>(false, status, message, errorCode, data);
    }
}
