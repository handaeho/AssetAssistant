package kr.daeho.AssetAssistant.users.dto;

public class CheckIdResponseDto {
    private boolean isDuplicate;

    public CheckIdResponseDto(boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
    }
}
