package kr.daeho.AssetAssistant.users.dto;

/**
 * 아이디 중복 체크 응답 DTO
 */
public class CheckIdResponseDto {
    /**
     * 아이디 중복 여부
     */
    private boolean isDuplicate;

    /**
     * 클래스 생성자
     * 
     * @param isDuplicate: 아이디 중복 여부
     */
    public CheckIdResponseDto(boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    /**
     * 아이디 중복 여부 반환
     * 
     * @return isDuplicate: 아이디 중복 여부
     */
    public boolean isDuplicate() {
        return isDuplicate;
    }

    /**
     * 아이디 중복 여부 설정
     * 
     * @param duplicate
     */
    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
    }
}
