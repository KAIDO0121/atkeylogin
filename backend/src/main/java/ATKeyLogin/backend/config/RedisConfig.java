package ATKeyLogin.backend.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
// import ATKeyLogin.backend.model.Emails;



@Configuration
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);


    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //参照StringRedisTemplate内部实现指定序列化器
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(keySerializer());
        redisTemplate.setHashKeySerializer(keySerializer());
        redisTemplate.setValueSerializer(valueSerializer());
        redisTemplate.setHashValueSerializer(valueSerializer());
        return redisTemplate;
    }

    private RedisSerializer<String> keySerializer(){
        return new StringRedisSerializer();
    }

    //使用Jackson序列化器
    private RedisSerializer<Object> valueSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }

    // public static class MyIndexConfiguration extends IndexConfiguration {

    //     @Override
    //     protected Iterable<IndexDefinition> initialConfiguration() {
    //         return Collections.singleton(new SimpleIndexDefinition("trialLicense", "licenseCode"));
    //     }
    // }

    // @Bean
    // public RedisMappingContext keyValueMappingContext() {
    //     return new RedisMappingContext(new MappingConfiguration(new MyIndexConfiguration(), new MyKeyspaceConfiguration()));
    // }

    // public static class MyKeyspaceConfiguration extends KeyspaceConfiguration {

    //     @Override
    //     protected Iterable<KeyspaceSettings> initialConfiguration() {
    //         KeyspaceSettings keyspaceSettings = new KeyspaceSettings(Emails.class, "trialLicense");
    //         keyspaceSettings.setTimeToLive(30L);
    //         return Collections.singleton(keyspaceSettings);
    //     }
    // }
    

    // @Component
    // public static class TrialLicenseExpiredEventListener {
    //     @EventListener
    //     public void handleRedisKeyExpiredEvent(RedisKeyExpiredEvent<Emails> event) {
    //         Emails expiredLicense = (Emails) event.getValue();
    //         try {
    //             log.debug("License with code={} has expired", expiredLicense.getLicenseCode());
    //         } catch (Exception e) {
    //             log.debug("e = {}", e);
    //         }
            
    //     }
    // }
}
