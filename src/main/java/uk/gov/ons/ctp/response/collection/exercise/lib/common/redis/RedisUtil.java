package uk.gov.ons.ctp.response.collection.exercise.lib.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil<o> {

  @Autowired
  @Qualifier("redisTemplate")
  private RedisTemplate<String, Object> redisTemplate;

  public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
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
