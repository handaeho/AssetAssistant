package kr.daeho.AssetAssistant.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import kr.daeho.AssetAssistant.common.constant.ErrorCode;

/**
 * 애플리케이션 기본 예외 클래스 -> 비즈니스 로직 레이어에서 사용 (서비스 레이어)
 * 
 * ErrorCode 열거형과 통합된 예외 처리 기반 클래스 -> 모든 비즈니스 예외는 이 클래스를 상속
 * 
 * RuntimeException을 상속해 예외 계층 구조를 만들고, 예외 코드와 메시지로 의미 명확하게 부여
 */
@Getter
public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String message;
    private final HttpStatus httpStatus;

    /**
     * ErrorCode를 사용한 기본 생성자
     */
    public ApplicationException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    /**
     * ErrorCode와 커스텀 메시지를 사용한 생성자
     */
    public ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = errorCode.getStatus();
    }

    /**
     * 원인 예외를 포함한 생성자
     */
    public ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = errorCode.getStatus();
    }

    /**
     * 사용자를 찾을 수 없을 때 발생하는 예외
     */
    public static class UserNotFoundException extends ApplicationException {
        public UserNotFoundException(String userId) {
            super(ErrorCode.USER_NOT_FOUND, String.format("사용자를 찾을 수 없습니다.: %s", userId));
        }
    }

    /**
     * 사용자 아이디 중복인 경우 발생하는 예외
     */
    public static class UserAlreadyExistsException extends ApplicationException {
        public UserAlreadyExistsException(String userId) {
            super(ErrorCode.USER_ALREADY_EXISTS, String.format("사용자 아이디가 이미 존재합니다.: %s", userId));
        }
    }

    /**
     * 로그인 실패 시 발생하는 예외
     */
    public static class LoginFailedException extends ApplicationException {
        public LoginFailedException(String userId) {
            super(ErrorCode.LOGIN_FAILED, String.format("로그인에 실패했습니다.: %s", userId));
        }
    }

    /**
     * 사용자 비밀번호 불일치 시 발생하는 예외
     */
    public static class UserPasswordNotMatchException extends ApplicationException {
        public UserPasswordNotMatchException(String userId) {
            super(ErrorCode.USER_PASSWORD_NOT_MATCH, String.format("사용자 비밀번호가 일치하지 않습니다.: %s", userId));
        }
    }

    // 계정 잠김 예외
    public static class AccountLockedException extends ApplicationException {
        public AccountLockedException(String message) {
            super(ErrorCode.ACCOUNT_LOCKED, message);
        }
    }

    // 계정 비활성화 예외
    public static class AccountDisabledException extends ApplicationException {
        public AccountDisabledException(String message) {
            super(ErrorCode.ACCOUNT_DISABLED, message);
        }
    }

    // 토큰 갱신 실패 예외
    public static class TokenRefreshFailedException extends ApplicationException {
        public TokenRefreshFailedException(String message) {
            super(ErrorCode.TOKEN_REFRESH_FAILED, message);
        }
    }

    /**
     * 회원가입 실패 시 발생하는 예외
     */
    public static class UserRegistrationFailedException extends ApplicationException {
        public UserRegistrationFailedException(Throwable cause) {
            super(ErrorCode.USER_REGISTRATION_FAILED, "회원가입에 실패했습니다.", cause);
        }
    }

    /**
     * 사용자 삭제 실패 시 발생하는 예외
     */
    public static class UserDeleteFailedException extends ApplicationException {
        public UserDeleteFailedException(Throwable cause) {
            super(ErrorCode.USER_DELETE_FAILED, "사용자 삭제에 실패했습니다.", cause);
        }
    }

    /**
     * 비밀번호 변경 실패 시 발생하는 예외
     */
    public static class PasswordUpdateFailedException extends ApplicationException {
        public PasswordUpdateFailedException(Throwable cause) {
            super(ErrorCode.PASSWORD_UPDATE_FAILED, "비밀번호 변경에 실패했습니다.", cause);
        }
    }

    /**
     * 자산 정보를 찾을 수 없을 때 발생하는 예외
     */
    public static class AssetsNotFoundException extends ApplicationException {
        public AssetsNotFoundException(String userId) {
            super(ErrorCode.ASSETS_NOT_FOUND, String.format("자산 정보를 찾을 수 없습니다.: %s", userId));
        }

        public AssetsNotFoundException(String userId, Throwable cause) {
            super(ErrorCode.ASSETS_NOT_FOUND, String.format("자산 정보를 찾을 수 없습니다.: %s", userId), cause);
        }
    }

    /**
     * 자산 정보 등록 실패 시 발생하는 예외
     */
    public static class AssetsCreateFailedException extends ApplicationException {
        public AssetsCreateFailedException(Throwable cause) {
            super(ErrorCode.ASSETS_CREATE_FAILED, "자산 정보 등록에 실패했습니다.", cause);
        }
    }

    /**
     * 자산 정보 삭제 실패 시 발생하는 예외
     */
    public static class AssetsDeleteFailedException extends ApplicationException {
        public AssetsDeleteFailedException(Throwable cause) {
            super(ErrorCode.ASSETS_DELETE_FAILED, "자산 정보 삭제에 실패했습니다.", cause);
        }
    }

    /**
     * 자산 정보 수정 실패 시 발생하는 예외
     */
    public static class AssetsUpdateFailedException extends ApplicationException {
        public AssetsUpdateFailedException(Throwable cause) {
            super(ErrorCode.ASSETS_UPDATE_FAILED, "자산 정보 수정에 실패했습니다.", cause);
        }
    }

    /**
     * 인증 실패 시 발생하는 예외
     */
    public static class AuthenticationFailedException extends ApplicationException {
        public AuthenticationFailedException() {
            super(ErrorCode.AUTHENTICATION_FAILED, "로그인에 실패했습니다.");
        }

        public AuthenticationFailedException(String message) {
            super(ErrorCode.AUTHENTICATION_FAILED, message);
        }

        public AuthenticationFailedException(Throwable cause) {
            super(ErrorCode.AUTHENTICATION_FAILED, "로그인에 실패했습니다.", cause);
        }
    }

    /**
     * 토큰이 유효하지 않을 때 발생하는 예외
     */
    public static class InvalidTokenException extends ApplicationException {
        public InvalidTokenException(String message) {
            super(ErrorCode.INVALID_TOKEN, message);
        }
    }

    /**
     * 토큰이 만료되었을 때 발생하는 예외
     */
    public static class TokenExpiredException extends ApplicationException {
        public TokenExpiredException(String message) {
            super(ErrorCode.TOKEN_EXPIRED, message);
        }
    }

    /**
     * 토큰 암호화 알고리즘이 일치하지 않을 때 발생하는 예외
     */
    public static class InvalidTokenAlgorithmException extends ApplicationException {
        public InvalidTokenAlgorithmException(String message) {
            super(ErrorCode.INVALID_TOKEN_ALGORITHM, message);
        }
    }

    /**
     * 토큰에서 사용자 아이디 추출 실패 시, 발생하는 예외
     */
    public static class GetUserIdFromTokenFailedException extends ApplicationException {
        public GetUserIdFromTokenFailedException(String message) {
            super(ErrorCode.USER_GET_FAILED_FROM_TOKEN, message);
        }
    }

    /**
     * 입력값 검증 실패 시 발생하는 예외
     */
    public static class ValidationFailedException extends ApplicationException {
        public ValidationFailedException(String field, String message) {
            super(ErrorCode.VALIDATION_ERROR, String.format("입력값 검증 실패.: %s - %s", field, message));
        }
    }

    /**
     * 권한이 없을 때 발생하는 예외
     */
    public static class AccessDeniedException extends ApplicationException {
        public AccessDeniedException() {
            super(ErrorCode.ACCESS_DENIED, "접근 권한이 없습니다.");
        }

        public AccessDeniedException(String message) {
            super(ErrorCode.ACCESS_DENIED, message);
        }
    }

    // NOTE: 다른 세분화된 예외 클래스들 추가해가며 작성
}