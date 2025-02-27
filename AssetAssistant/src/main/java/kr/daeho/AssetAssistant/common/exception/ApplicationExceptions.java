package kr.daeho.AssetAssistant.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 기본 예외 클래스 -> 예외 유형 정의
 * 
 * 애플리케이션에서 발생할 수 있는 다양한 비즈니스 로직의 예외를 정의
 * 
 * RuntimeException을 상속해 예외 계층 구조를 만들고, 예외 코드와 메시지로 의미 명확하게 부여
 * 
 * 각 예외는 고유한 에러 코드와 HTTP 상태 코드를 가지며, 일관된 방식으로 처리됨
 */
@Getter
public class ApplicationExceptions extends RuntimeException {
    /**
     * 에러 코드 (예: "USER_NOT_FOUND", "AUTHENTICATION_FAILED" 등)
     */
    private final String errorCode;

    /**
     * HTTP 상태 코드 - 기본값은 500 (내부 서버 오류)
     */
    private final HttpStatus httpStatus;

    /**
     * 기본 생성자 - 에러 코드와 메시지만 지정, HTTP 상태는 500으로 설정
     */
    public ApplicationExceptions(String errorCode, String message) {
        this(errorCode, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * HTTP 상태 코드를 포함한 생성자
     */
    public ApplicationExceptions(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * 원인 예외를 포함한 생성자
     */
    public ApplicationExceptions(String errorCode, String message, Throwable cause) {
        this(errorCode, message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    /**
     * 모든 속성을 지정하는 생성자
     */
    public ApplicationExceptions(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * 사용자를 찾을 수 없을 때 발생하는 예외
     */
    public static class UserNotFoundException extends ApplicationExceptions {
        public UserNotFoundException(String userId) {
            super("USER_NOT_FOUND",
                    String.format("사용자를 찾을 수 없습니다: %s", userId),
                    HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 사용자 아이디 중복인 경우 발생하는 예외
     */
    public static class UserAlreadyExistsException extends ApplicationExceptions {
        public UserAlreadyExistsException(String userId) {
            super("USER_ALREADY_EXISTS",
                    String.format("사용자 아이디가 이미 존재합니다: %s", userId),
                    HttpStatus.CONFLICT);
        }
    }

    /**
     * 사용자 비밀번호 불일치 시 발생하는 예외
     */
    public static class UserPasswordNotMatchException extends ApplicationExceptions {
        public UserPasswordNotMatchException(String userId) {
            super("USER_PASSWORD_NOT_MATCH",
                    String.format("사용자 비밀번호가 일치하지 않습니다: %s", userId),
                    HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 자산 정보를 찾을 수 없을 때 발생하는 예외
     */
    public static class AssetsNotFoundException extends ApplicationExceptions {
        public AssetsNotFoundException(String userId) {
            super("ASSETS_NOT_FOUND",
                    String.format("자산 정보를 찾을 수 없습니다: %s", userId),
                    HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 인증 실패 시 발생하는 예외
     */
    public static class AuthenticationFailedException extends ApplicationExceptions {
        public AuthenticationFailedException() {
            super("AUTHENTICATION_FAILED",
                    "로그인에 실패했습니다",
                    HttpStatus.UNAUTHORIZED);
        }

        public AuthenticationFailedException(String message) {
            super("AUTHENTICATION_FAILED",
                    message,
                    HttpStatus.UNAUTHORIZED);
        }

        public AuthenticationFailedException(Throwable cause) {
            super("AUTHENTICATION_FAILED",
                    "로그인에 실패했습니다",
                    HttpStatus.UNAUTHORIZED,
                    cause);
        }
    }

    /**
     * 토큰이 유효하지 않을 때 발생하는 예외
     */
    public static class InvalidTokenException extends ApplicationExceptions {
        public InvalidTokenException(String message) {
            super("INVALID_TOKEN",
                    message,
                    HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 토큰이 만료되었을 때 발생하는 예외
     */
    public static class TokenExpiredException extends ApplicationExceptions {
        public TokenExpiredException() {
            super("TOKEN_EXPIRED",
                    "토큰이 만료되었습니다",
                    HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 입력값 검증 실패 시 발생하는 예외
     */
    public static class ValidationFailedException extends ApplicationExceptions {
        public ValidationFailedException(String field, String message) {
            super("VALIDATION_FAILED",
                    String.format("입력값 검증 실패: %s - %s", field, message),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 권한이 없을 때 발생하는 예외
     */
    public static class AccessDeniedException extends ApplicationExceptions {
        public AccessDeniedException() {
            super("ACCESS_DENIED",
                    "접근 권한이 없습니다",
                    HttpStatus.FORBIDDEN);
        }

        public AccessDeniedException(String message) {
            super("ACCESS_DENIED",
                    message,
                    HttpStatus.FORBIDDEN);
        }
    }

    // NOTE: 다른 세분화된 예외 클래스들 추가해가며 작성
}