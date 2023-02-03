package uk.gov.ons.ctp.response.collection.exercise.lib.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class RedisUtil<T> {

  private static final String REDIS_KEY_PREFIX = "frontstage:collection-exercise-by-survey-id:";


  private final RedisTemplate<String, T> redisTemplate;
  private HashOperations<String, Object, T> hashOperation;
  private ListOperations<String, T> listOperation;
  private ValueOperations<String, T> valueOperations;

  @Autowired
  public RedisUtil(RedisTemplate<String, T> redisTemplate) {
    this.hashOperation = redisTemplate.opsForHash();
    this.listOperation = redisTemplate.opsForList();
    this.valueOperations = redisTemplate.opsForValue();
    this.redisTemplate = redisTemplate;
  }

  public void removeFromList(String redisKey, long count, Object key) {
    listOperation.remove(redisKey, count, key);
  }

  public void putMap(String redisKey, Object key, T data) {
    hashOperation.put(redisKey, key, data);
  }

  public T getMapAsSingleEntry(String redisKey, Object key) {
    return hashOperation.get(redisKey, key);
  }

  public Map<Object, T> getMapAsAll(String redisKey) {
    return hashOperation.entries(redisKey);
  }

  public void putValue(String key, T value) {
    valueOperations.set(key, value);
  }

  public void putValueWithExpireTime(String key, T value, long timeout, TimeUnit unit) {
    valueOperations.set(key, value, timeout, unit);
  }

  public T getValue(String key) {
    return valueOperations.get(key);
  }

  public void setExpire(String key, long timeout, TimeUnit unit) {
    redisTemplate.expire(key, timeout, unit);
  }

  public void deleteValue(String key) {
    redisTemplate.delete(key);
  }

}
