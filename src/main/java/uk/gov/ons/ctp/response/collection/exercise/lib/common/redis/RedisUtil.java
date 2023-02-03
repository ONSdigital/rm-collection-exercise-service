package uk.gov.ons.ctp.response.collection.exercise.lib.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisUtil<Object> {

  private static final String REDIS_KEY_PREFIX = "frontstage:collection-exercise-by-survey-id:";

  @Autowired private RedisTemplate<String, String> redisTemplate;

  public void deleteValue(String key) {
    redisTemplate.delete(key);
  }
}
