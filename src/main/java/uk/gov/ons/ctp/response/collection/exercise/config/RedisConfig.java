package uk.gov.ons.ctp.response.collection.exercise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  JedisConnectionFactory jedisConnectionFactory() {
    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("localhost", 6379);
    configuration.setDatabase(1);
    JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().usePooling().build();
    JedisConnectionFactory factory = new JedisConnectionFactory(configuration, jedisClientConfiguration);
    factory.afterPropertiesSet();
    return factory;
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setDefaultSerializer(new StringRedisSerializer());
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new GenericToStringSerializer<>(Object.class));
    template.setHashValueSerializer(new JdkSerializationRedisSerializer());
    template.setValueSerializer(new JdkSerializationRedisSerializer());
    template.setConnectionFactory(jedisConnectionFactory());
    return template;
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return jedisConnectionFactory();
  }
}
