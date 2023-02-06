package uk.gov.ons.ctp.response.collection.exercise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

  @Bean
  protected JedisConnectionFactory jedisConnectionFactory() {
    RedisStandaloneConfiguration configuration =
        new RedisStandaloneConfiguration("localhost", 6379);
    configuration.setDatabase(3);
    JedisClientConfiguration jedisClientConfiguration =
        JedisClientConfiguration.builder().usePooling().build();
    JedisConnectionFactory factory =
        new JedisConnectionFactory(configuration, jedisClientConfiguration);
    factory.afterPropertiesSet();
    return factory;
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      JedisConnectionFactory jedisConnectionFactory) {
    final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(jedisConnectionFactory);
    return redisTemplate;
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return jedisConnectionFactory();
  }
}
