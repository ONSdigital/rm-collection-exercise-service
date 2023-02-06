package uk.gov.ons.ctp.response.collection.exercise.lib.common.redis;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisUtil<Object> {

  private final RedisTemplate<String, String> redisTemplate;

  public RedisUtil(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public Boolean doesKeyExist(String key) {
    System.out.println("CHECKING KEY !!");
    System.out.println("CHECKING KEY !!" + key);
    System.out.println("CHECKING KEY !!");
    System.out.println("CHECKING KEY !!");
    return redisTemplate.hasKey(key);
  }

  public void deleteValue(String key) {
    redisTemplate.delete(key);
    System.out.println("DELETE KEY !!!");
    System.out.println("DELETE KEY !!!" + key);
    System.out.println("DELETE KEY !!!");
    System.out.println("DELETE KEY !!!");
  }
}
