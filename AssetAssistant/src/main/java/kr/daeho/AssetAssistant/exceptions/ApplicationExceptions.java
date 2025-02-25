package kr.daeho.AssetAssistant.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import lombok.Getter;

@Getter
public class ApplicationExceptions extends RuntimeException {
    /**
     * 에러 코드 (ex: "USER_NOT_FOUND", "AUTHENTICATION_FAILED" 등)
     */
    private final String errorCode;

    /**
     * 에러 코드와 메시지를 이용하여 ApplicationException을 생성
     *
     * @param errorCode 예외 식별을 위한 에러 코드
     * @param message   상세 예외 메시지
     */
    public ApplicationExceptions(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 에러 코드, 메시지, 그리고 원인 예외를 이용하여 ApplicationException을 생성
     *
     * @param errorCode 예외 식별을 위한 에러 코드
     * @param message   상세 예외 메시지
     * @param cause     원인 예외
     */
    public ApplicationExceptions(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 사용자가 검색한 정보를 찾을 수 없을 때 발생하는 예외
     * 
     * @ResponseStatus(HttpStatus.NOT_FOUND): 예외 발생 시 HTTP 404 응답 반환
     * - REST API의 일관성 유지
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFoundException extends ApplicationExceptions {
        public NotFoundException(String message) {
            super("NOT_FOUND", message);
        }
    }

    /**
     * 현재 예외의 에러 코드를 반환합니다.
     *
     * @return errorCode
     */
    public String getErrorCode() {
        return errorCode;
    }
}
