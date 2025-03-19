package kr.daeho.AssetAssistant.common.utils;

import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * Jasypt 암호화 설정 유틸리티 클래스
 * 
 * Jasypt 암호화에 필요한 설정 정보를 제공하는 유틸리티 클래스
 * JasyptConfigAES에서 암호화 설정을 위해 사용됨
 * 
 * 역할: 암호화 설정 정보 제공
 * 
 * 주요 기능:
 * - 암호화 알고리즘, 키 생성 방식 등 암호화에 필요한 설정 제공
 * - JasyptConfigAES가 이 클래스의 설정 정보를 활용
 */
public class JasyptEncryptionUtil {
    // 암호화 알고리즘
    public static final String ALGORITHM = "PBEWITHHMACSHA512ANDAES_256";
    // 키 스트레칭 (반복할 해싱 연산 횟수)
    public static final String KEY_OBTENTION_ITERATIONS = "1000";
    // 인스턴스 pool 크기 (1: 단일 인스턴스)
    public static final String POOL_SIZE = "1";
    // 자바 암호화 확장 제공자 이름 (Sun 제공의 JCE 암호화 확장)
    public static final String PROVIDER_NAME = "SunJCE";
    // salt 생성 클래스 (랜덤 솔트 생성)
    public static final String SALT_GENERATOR_CLASS_NAME = "org.jasypt.salt.RandomSaltGenerator";
    // 블록 암호 알고리즘의 초기화 벡터(IV) 생성 클래스 (랜덤 IV 생성)
    public static final String IV_GENERATOR_CLASS_NAME = "org.jasypt.iv.RandomIvGenerator";
    // 출력 인코딩 방식 (Base64 인코딩)
    public static final String STRING_OUTPUT_TYPE = "base64";
    // 마스터 키 기본값
    public static final String DEFAULT_MASTER_PASSWORD = "secret-master-password";

    /**
     * Jasypt 암호화 설정 객체 생성 메소드
     * 
     * @param masterPassword 마스터 키
     * @return 구성 설정 객체
     */
    public static SimpleStringPBEConfig createEncryptorConfig(String masterPassword) {
        // 구성 설정 객체 초기화
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        // 구성 설정 객체에 적용
        config.setPassword(masterPassword);
        config.setAlgorithm(ALGORITHM);
        config.setKeyObtentionIterations(KEY_OBTENTION_ITERATIONS);
        config.setPoolSize(POOL_SIZE);
        config.setProviderName(PROVIDER_NAME);
        config.setSaltGeneratorClassName(SALT_GENERATOR_CLASS_NAME);
        config.setIvGeneratorClassName(IV_GENERATOR_CLASS_NAME);
        config.setStringOutputType(STRING_OUTPUT_TYPE);

        return config;
    }
}