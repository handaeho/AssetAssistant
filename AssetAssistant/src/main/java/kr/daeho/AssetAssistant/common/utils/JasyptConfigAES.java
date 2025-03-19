package kr.daeho.AssetAssistant.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

/**
 * Jasypt 설정 및 암호화, 복호화 클래스
 * 
 * 역할: 실제 암호화/복호화 수행
 * 
 * 주요 기능:
 * - 스프링 빈으로 등록되어 애플리케이션의 암호화된 속성 복호화 수행
 * - 외부에 암호화/복호화 기능 제공
 * - 다른 클래스와의 관계: JasyptEncryptionUtil의 설정 정보를 사용
 * 
 * @Configuration: 설정 정의 및 빈 정의 등 설정 관련 클래스를 위한 특수 목적의 어노테이션
 *                 - @Component의 특수화된 형태 (@Component: 범용적인 빈 등록을 위한 어노테이션)
 *                 - 내부에서 싱글톤 보장을 위한 CGLIB 프록시를 사용하기 위함
 *                 -> 내부의 @Bean 메서드 호출을 가로채어 싱글톤 보장을 위해 CGLIB 프록시를 적용해서,
 *                 -> @Configuration를 적용한 클래스 내에서 @Bean이 적용된 메소드를 통해 빈을 생성하고,
 *                 -> 이 메소드를 여러 번 요청해도 같은 인스턴스가 반환되는 것을 보장
 * @EnableEncryptableProperties: jasypt-spring-boot 라이브러리의 암호화/복호화 어노테이션
 *                               - 암호화된 프로퍼티를 인식하고 자동으로 복호화
 * @RequiredArgsConstructor: 생성자 주입 방식을 사용하기 위한 어노테이션 (@Autowired 대신 사용)
 *                           (final 및 notNull 필드에 대한 생성자 자동 생성)
 * @Slf4j: 로깅을 위한 어노테이션
 */
@Configuration
@EnableEncryptableProperties
@RequiredArgsConstructor
@Slf4j
public class JasyptConfigAES {
    // 시스템 프로퍼티(-D 옵션)나 OS 환경 변수, 커맨드 라인 파라미터 등을 통합적으로 관리하는 인터페이스 주입
    private final Environment environment;

    // 시스템 프로퍼티나 커맨드라인 파라미터의 마스터 키 상수
    private static final String MASTER_KEY_PARAMETER = "jasypt.master.key";

    /**
     * application.yml 설정 파일의 속성 암호화/복호화 설정 (DB 정보, JWT 비밀키 등)
     * 
     * @Bean: 빈 등록 어노테이션
     *        - 메서드에 @Bean 어노테이션을 붙이면, 해당 메서드가 반환하는 객체를 스프링 컨테이너에 빈으로 등록
     *        - 빈 이름은 메서드 이름이 되며, 메서드 이름이 중복되면 오류 발생
     *        - 빈 이름을 지정하려면 @Bean(name = "빈 이름") 형식으로 사용
     * 
     * @return StringEncryptor: 문자열 암호화/복호화 인터페이스
     */
    @Bean("jasyptEncryptorAES")
    public StringEncryptor stringEncryptor() {
        // 마스터 키를 커맨드라인 파라미터에서 가져옴
        String masterPassword = getMasterPassword();

        // 암호화기 초기화 및 설정
        PooledPBEStringEncryptor encryptor = createEncryptor(masterPassword);

        // 설정이 완료된 PooledPBEStringEncryptor 인스턴스 반환
        return encryptor;
    }

    /**
     * 마스터 비밀번호를 커맨드라인 파라미터에서 가져오는 메서드
     * 
     * @return 마스터 비밀번호
     */
    private String getMasterPassword() {
        // 커맨드라인 파라미터에서 마스터 키 확인
        String password = environment.getProperty(MASTER_KEY_PARAMETER);

        // 마스터 키가 없으면 기본값 사용
        if (password == null || password.isEmpty()) {
            password = JasyptEncryptionUtil.DEFAULT_MASTER_PASSWORD;
            log.warn("⚠️ 주의: 암호화 마스터 키가 제공되지 않았습니다. 기본값이 사용됩니다.");
            log.warn("애플리케이션 실행 시 --{}=마스터키 형식으로 파라미터를 전달하세요.", MASTER_KEY_PARAMETER);
        } else {
            log.info("Jasypt 암호화 마스터 키가 커맨드 라인 파라미터에서 로드되었습니다.");
        }

        return password;
    }

    /**
     * 지정된 마스터 키를 사용하여 암호화기 객체를 생성하는 메소드
     * 
     * @param masterPassword 마스터 키
     * @return 암호화기 객체
     */
    private PooledPBEStringEncryptor createEncryptor(String masterPassword) {
        // PBE(Password-Based Encryption) 방식의 문자열 암호화기 초기화
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();

        // JasyptEncryptionUtil에서 제공하는 createEncryptorConfig 메서드를 통해 설정 객체 생성
        SimpleStringPBEConfig config = JasyptEncryptionUtil.createEncryptorConfig(masterPassword);

        // 구성 설정 적용
        encryptor.setConfig(config);

        return encryptor;
    }

    /**
     * 지정된 마스터 키로 값을 암호화하는 메소드
     * 
     * @param value          암호화할 값
     * @param masterPassword 마스터 키
     * @return 암호화된 값
     */
    public static String encrypt(String value, String masterPassword) {
        // 암호화기 생성
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();

        // JasyptEncryptionUtil에서 제공하는 createEncryptorConfig 메서드를 통해 설정 객체 생성
        SimpleStringPBEConfig config = JasyptEncryptionUtil.createEncryptorConfig(masterPassword);

        // 구성 설정 적용
        encryptor.setConfig(config);

        // 암호화 수행
        return encryptor.encrypt(value);
    }

    /**
     * 지정된 마스터 키로 값을 복호화하는 메소드
     * 
     * @param encryptedValue 암호화된 값
     * @param masterPassword 마스터 키
     * @return 복호화된 원본 값
     */
    public static String decrypt(String encryptedValue, String masterPassword) {
        // 암호화기 생성
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();

        // JasyptEncryptionUtil에서 제공하는 createEncryptorConfig 메서드를 통해 설정 객체 생성
        SimpleStringPBEConfig config = JasyptEncryptionUtil.createEncryptorConfig(masterPassword);

        // 구성 설정 적용
        encryptor.setConfig(config);

        // 복호화 수행
        return encryptor.decrypt(encryptedValue);
    }
}