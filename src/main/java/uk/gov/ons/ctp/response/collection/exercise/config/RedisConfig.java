package uk.gov.ons.ctp.response.collection.exercise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    //    RedisStandaloneConfiguration redisStandaloneConfiguration =
    //        new RedisStandaloneConfiguration("localhost", 6379);
    //    // this is 3 in dev will need to confirm in other envs
    //    redisStandaloneConfiguration.setDatabase(3);
    //    return new LettuceConnectionFactory(redisStandaloneConfiguration);
    return new LettuceConnectionFactory();
  }

  @Bean
  RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    return template;
  }
}
