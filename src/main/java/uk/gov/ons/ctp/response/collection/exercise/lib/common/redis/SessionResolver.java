package uk.gov.ons.ctp.response.collection.exercise.lib.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionResolver {

  private static final String REDIS_KEY_PREFIX = "frontstage:collection-exercise-by-survey-id:";


  @Autowired private RedisUtil<Object> redisUtil;

  public void removeActiveSession(String surveyId) {
    redisUtil.deleteValue(REDIS_KEY_PREFIX + surveyId);
  }
}
