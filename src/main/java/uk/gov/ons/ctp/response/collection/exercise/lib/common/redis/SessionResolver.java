package uk.gov.ons.ctp.response.collection.exercise.lib.common.redis;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionResolver {

  private static final Logger log = LoggerFactory.getLogger(SessionResolver.class);

  private static final String REDIS_KEY_PREFIX = "frontstage:collection-exercise-by-survey-id:";

  @Autowired private RedisUtil<Object> redisUtil;

  public void removeActiveSession(String surveyId) {
    String key = REDIS_KEY_PREFIX + surveyId;
    Boolean cacheInvalidated = redisUtil.deleteValue(key);
    log.with("key", key).with("success", cacheInvalidated).info("Cache invalidated with key");
  }
}
