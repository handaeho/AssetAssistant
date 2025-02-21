package kr.daeho.AssetAssistant.assets.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class AssetsException extends RuntimeException {
    // 오류 식별 코드 (예: ASSETS_NOT_FOUND, INVALID_ASSETS_ID 등)
    private final String errorCode;
    // 오류 상세 설명
    private final String errorDetail;

    public AssetsException(String errorCode, String errorDetail) {
        // super: 부모 클래스(RuntimeException)의 생성자 호출
        // RuntimeException의 생성자에 errorDetail 전달
        super(errorDetail);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    public AssetsException(String errorCode, String errorDetail, Throwable cause) {
        // 필요에 따라 errorDetail과 원인(cause)를 함께 전달
        super(errorDetail, cause);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    /**
     * 자산 정보를 찾을 수 없을 때 발생하는 예외
     * 
     * @ResponseStatus(HttpStatus.NOT_FOUND): 예외 발생 시 HTTP 404 응답 반환
     * - REST API의 일관성 유지
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class AssetNotFoundException extends AssetsException {
        public AssetNotFoundException(String message) {
            super("ASSET_NOT_FOUND", message);
        }
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

}
