package kr.daeho.AssetAssistant.common.utils;

/**
 * Gradle의 run 태스크를 사용한 Jasypt 암호화 실행을 위한 Runner 클래스
 * 
 * JasyptRunner <-> JasyptConfigAES(암호화 수행) <-> JasyptEncryptionUtil(설정 정보)
 */
public class JasyptRunner {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("암호화할 값을 지정하세요.");
            System.out.println("사용법: ./gradlew encryptValue -Pvalue=암호화할_값 [-Pkey=마스터_키]");
            System.out.println("암호화할 값은 필수 파라미터. 마스터키는 미입력시 기본값 사용");
            return;
        }

        // 첫 번째 인자: 암호화할 값
        String valueToEncrypt = args[0];

        // 두 번째 인자(옵션): 마스터 키, 없으면 기본값 사용
        String masterKey = args.length > 1 ? args[1] : JasyptEncryptionUtil.DEFAULT_MASTER_PASSWORD;

        // JasyptConfigAES를 통해 암호화 수행
        String encryptedValue = JasyptConfigAES.encrypt(valueToEncrypt, masterKey);

        // 결과 출력
        System.out.println("\n====== Jasypt 암호화 결과 ======");
        System.out.println("application.yml에 적용: ENC(" + encryptedValue + ")");

        // 복호화 테스트 (검증용)
        String decryptedValue = JasyptConfigAES.decrypt(encryptedValue, masterKey);
        System.out.println("원본 값과 일치: " + valueToEncrypt.equals(decryptedValue));
    }
}