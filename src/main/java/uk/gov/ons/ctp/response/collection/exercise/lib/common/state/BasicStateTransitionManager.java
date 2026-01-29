package uk.gov.ons.ctp.response.collection.exercise.lib.common.state;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;

/**
 * A Simple impl of StateTransitionManager
 *
 * @param <S> The state type we transit from and to
 * @param <E> The event type that effects the transition
 */
@Data
@Getter
public class BasicStateTransitionManager<S, E> implements StateTransitionManager<S, E> {

  private static final Logger log = LoggerFactory.getLogger(BasicStateTransitionManager.class);

  public static final String TRANSITION_ERROR_MSG = "State Transition from %s via %s is forbidden.";

  private Map<S, Map<E, S>> transitions = new HashMap<>();

  /**
   * Construct the instance with a provided map of transitions
   *
   * @param transitionMap the transitions
   */
  public BasicStateTransitionManager(final Map<S, Map<E, S>> transitionMap) {
    transitions = transitionMap;
  }

  @Override
  public S transition(final S sourceState, final E event) throws CTPException {
    S destinationState = null;
    Map<E, S> outputMap = transitions.get(sourceState);
    if (outputMap != null) {
      destinationState = outputMap.get(event);
    }
    if (destinationState == null) {
      log.warn(
          "No valid transition", kv("from", sourceState.toString()), kv("event", event.toString()));
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST, String.format(TRANSITION_ERROR_MSG, sourceState, event));
    } else {
      log.info(
          "Transitioning state",
          kv("from", sourceState.toString()),
          kv("to", destinationState.toString()),
          kv("event", event.toString()));
    }
    return destinationState;
  }

  @Override
  public Map<E, S> getAvailableTransitions(final S sourceState) {
    Map<E, S> outputMap = transitions.get(sourceState);
    if (outputMap != null) {
      return outputMap;
    }
    return Collections.emptyMap();
  }
}
