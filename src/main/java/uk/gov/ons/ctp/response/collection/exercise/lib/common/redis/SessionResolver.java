package uk.gov.ons.ctp.response.collection.exercise.lib.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionResolver {

  @Autowired private RedisUtil<Object> redisUtil;

  public void removeActivesSession(String exerciseId) {
    redisUtil.deleteValue(exerciseId);
  }
}
