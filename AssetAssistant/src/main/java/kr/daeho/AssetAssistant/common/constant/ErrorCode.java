package kr.daeho.AssetAssistant.common.constant;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 애플리케이션 전체 에러 코드 정의
 * 
 * 모든 에러 코드를 한 곳에서 관리하여 일관성 유지
 * - 코드: 고유한 에러 코드 문자열
 * - 메시지: 기본 에러 메시지
 * - 상태: 연관된 HTTP 상태 코드
 * 
 * @Getter: 모든 필드의 Getter 메서드를 자동으로 생성. setter는 의도적으로 제외하여 불변성 유지
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션 (@Autowired 대신 사용)
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 일반 오류
    INTERNAL_ERROR("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("INVALID_REQUEST", "유효하지 않은 요청입니다", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("VALIDATION_ERROR", "입력값 검증에 실패했습니다", HttpStatus.BAD_REQUEST),

    // 사용자 관련 오류
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "사용자가 이미 존재합니다", HttpStatus.CONFLICT),
    USER_PASSWORD_NOT_MATCH("USER_PASSWORD_NOT_MATCH", "비밀번호가 일치하지 않습니다", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", "계정이 잠겼습니다", HttpStatus.FORBIDDEN),
    USER_REGISTRATION_FAILED("USER_REGISTRATION_FAILED", "회원가입 실패", HttpStatus.BAD_REQUEST),
    USER_DELETE_FAILED("USER_DELETE_FAILED", "사용자 삭제 실패", HttpStatus.BAD_REQUEST),
    PASSWORD_UPDATE_FAILED("PASSWORD_UPDATE_FAILED", "비밀번호 변경 실패", HttpStatus.BAD_REQUEST),

    // 자산 관련 오류
    ASSETS_NOT_FOUND("ASSETS_NOT_FOUND", "자산 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ASSETS_CREATE_FAILED("ASSETS_CREATE_FAILED", "자산 정보 등록 실패", HttpStatus.BAD_REQUEST),
    ASSETS_UPDATE_FAILED("ASSETS_UPDATE_FAILED", "자산 정보 수정 실패", HttpStatus.BAD_REQUEST),
    ASSETS_DELETE_FAILED("ASSETS_DELETE_FAILED", "자산 정보 삭제 실패", HttpStatus.BAD_REQUEST),

    // 인증 관련 오류
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "인증에 실패했습니다", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "토큰이 만료되었습니다", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("ACCESS_DENIED", "접근 권한이 없습니다", HttpStatus.FORBIDDEN),
    USER_GET_FAILED_FROM_TOKEN("USER_GET_FAILED_FROM_TOKEN",
            "토큰에서 사용자 아이디를 추출할 수 없습니다.",
            HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_ALGORITHM("INVALID_TOKEN_ALGORITHM", "토큰 암호화 알고리즘이 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;

    /**
     * 에러 코드 문자열로 ErrorCode 조회
     * 
     * @param code 에러 코드 문자열
     * @return 해당 ErrorCode 또는 없으면 INTERNAL_ERROR
     */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return INTERNAL_ERROR;
    }
}
