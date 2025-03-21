package kr.daeho.AssetAssistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

/**
 * Spring에서의 Redis 설정 클래스
 * 
 * 주요 기능:
 * - LettuceConnectionFactory 빈 생성
 * - RedisTemplate 빈 생성
 * - ConfigureRedisAction 빈 생성
 * 
 * @Configuration: 설정 정의 및 빈 정의 등 설정 관련 클래스를 위한 특수 목적의 어노테이션
 *                 - @Component의 특수화된 형태 (@Component: 범용적인 빈 등록을 위한 어노테이션)
 *                 - 내부에서 싱글톤 보장을 위한 CGLIB 프록시를 사용하기 위함
 *                 -> 내부의 @Bean 메서드 호출을 가로채어 싱글톤 보장을 위해 CGLIB 프록시를 적용해서,
 *                 -> @Configuration를 적용한 클래스 내에서 @Bean이 적용된 메소드를 통해 빈을 생성하고,
 *                 -> 이 메소드를 여러 번 요청해도 같은 인스턴스가 반환되는 것을 보장
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    /**
     * LettuceConnectionFactory 빈 생성
     * 
     * LettuceConnectionFactory: Redis 연결 관리 클래스
     * 
     * Redis 서버와의 통신을 위한 연결 인스턴스를 제공
     * 
     * 설정에 따라서 새로운 RedisConnection 또는 이미 존재하는 RedisConnection을 리턴
     * 
     * RedisTemplate이나 다른 Redis 관련 컴포넌트들이 Redis 서버에 접근할 수 있도록,
     * 내부적으로 RedisConnection을 생성할 때 사용
     * 
     * 핵심) Redis 서버와의 연결 설정(예: 호스트, 포트, 인증정보 등)을 한 곳에서 관리 가능
     * 
     * @return LettuceConnectionFactory 인스턴스
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    /**
     * RedisTemplate 빈 생성
     * 
     * RedisTemplate: Redis 연결 및 데이터 조작을 위한 템플릿 클래스
     * 
     * Redis와의 데이터 교환을 쉽게 해주는 도우미(템플릿) 클래스
     * 
     * 다양한 데이터 타입(String, Hash, List 등)에 대해 CRUD 작업을 간편하게 수행 도움
     * 
     * RedisTemplate을 통해 복잡한 Redis 명령어를 직접 작성하지 않고, 메서드 호출만으로 데이터 접근 가능
     * 
     * 내부적으로는 RedisConnectionFactory를 사용해 Redis 서버와 연결
     * 
     * 핵심) 직렬화/역직렬화 설정을 통해 객체를 쉽게 저장하고 관리 가능
     * 
     * @return RedisTemplate 인스턴스
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // 키는 문자열로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 값은 JSON으로 직렬화
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * ConfigureRedisAction 빈 생성
     * 
     * ConfigureRedisAction: Redis 설정 조정을 위한 빈 클래스
     * 
     * Spring Session 등과 Redis를 함께 사용할 때,
     * Redis 서버에 특정 **관리 명령어(CONFIG 등)**를 보내는 동작을 설정
     * 
     * 일부 클라우드나 보안 환경에서는 관리 명령어를 제한하는 경우가 있는데,
     * 이때, ConfigureRedisAction.NO_OP 을 설정하면 관리 명령어를 전달하지 않음
     * 
     * 핵심) 환경에 따라 Redis에 관리 명령어를 보내는 동작을 제어 (Redis 설정 문제 오류 방지)
     * 
     * @return ConfigureRedisAction 인스턴스
     */
    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
}