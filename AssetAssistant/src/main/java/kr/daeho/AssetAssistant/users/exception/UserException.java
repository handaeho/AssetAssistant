package kr.daeho.AssetAssistant.users.exception;

public class UserException extends RuntimeException {
    // 오류 식별 코드 (예: USER_NOT_FOUND, INVALID_USER_ID 등)
    private final String errorCode;
    // 오류 상세 설명
    private final String errorDetail;

    public UserException(String errorCode, String errorDetail) {
        // super: 부모 클래스(RuntimeException)의 생성자 호출
        // RuntimeException의 생성자에 errorDetail 전달
        super(errorDetail);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    public UserException(String errorCode, String errorDetail, Throwable cause) {
        // 필요에 따라 errorDetail과 원인(cause)를 함께 전달
        super(errorDetail, cause);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDetail() {
        return errorDetail;
    }
}
