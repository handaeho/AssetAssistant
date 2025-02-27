package kr.daeho.AssetAssistant.common.exception;

import lombok.Getter;

/**
 * 애플리케이션 기본 예외 클래스 -> 예외 유형 정의
 * 
 * 애플리케이션에서 발생할 수 있는 다양한 비즈니스 로직의 예외를 정의
 * 
 * RuntimeException을 상속해 예외 계층 구조를 만들고, 예외 코드와 메시지로 의미 명확하게 부여
 */
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
     * 사용자를 찾을 수 없을 때 발생하는 예외
     */
    public static class UserNotFoundException extends ApplicationExceptions {
        public UserNotFoundException(String userId) {
            super("USER_NOT_FOUND", String.format("사용자를 찾을 수 없습니다: %s", userId));
        }
    }

    /**
     * 사용자 아이디 중복인 경우 발생하는 예외
     */
    public static class UserAlreadyExistsException extends ApplicationExceptions {
        public UserAlreadyExistsException(String userId) {
            super("USER_ALREADY_EXISTS", String.format("사용자 아이디가 이미 존재합니다: %s", userId));
        }
    }

    /**
     * 사용자 비밀번호 불일치 시 발생하는 예외
     */
    public static class UserPasswordNotMatchException extends ApplicationExceptions {
        public UserPasswordNotMatchException(String userId) {
            super("USER_PASSWORD_NOT_MATCH", String.format("사용자 비밀번호가 일치하지 않습니다: %s", userId));
        }
    }

    /**
     * 자산 정보를 찾을 수 없을 때 발생하는 예외
     */
    public static class AssetsNotFoundException extends ApplicationExceptions {
        public AssetsNotFoundException(String userId) {
            super("ASSETS_NOT_FOUND", String.format("자산 정보를 찾을 수 없습니다: %s", userId));
        }
    }

    /**
     * 인증 실패 시 발생하는 예외
     */
    public static class AuthenticationFailedException extends ApplicationExceptions {
        public AuthenticationFailedException() {
            super("AUTHENTICATION_FAILED", "로그인에 실패했습니다");
        }

        public AuthenticationFailedException(Throwable cause) {
            super("AUTHENTICATION_FAILED", "로그인에 실패했습니다", cause);
        }
    }

    /**
     * 토큰이 유효하지 않을 때 발생하는 예외
     */
    public static class InvalidTokenException extends ApplicationExceptions {
        public InvalidTokenException(String message) {
            super("INVALID_TOKEN", message);
        }
    }

    /**
     * 토큰이 만료되었을 때 발생하는 예외
     */
    public static class TokenExpiredException extends ApplicationExceptions {
        public TokenExpiredException() {
            super("TOKEN_EXPIRED", "토큰이 만료되었습니다");
        }
    }

    /**
     * 접근 권한이 없을 때 발생하는 예외
     */
    public static class AccessDeniedException extends ApplicationExceptions {
        public AccessDeniedException() {
            super("ACCESS_DENIED", "이 리소스에 접근할 권한이 없습니다");
        }

        public AccessDeniedException(String resource) {
            super("ACCESS_DENIED", String.format("리소스 %s에 접근할 권한이 없습니다", resource));
        }
    }

    // NOTE: 다른 세분화된 예외 클래스들 추가해가며 작성
}