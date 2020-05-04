package uk.gov.ons.ctp.response.collection.exercise.lib.common.state;

import java.util.Map;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;

/**
 * A template interface for a state transition manager that will map valid transitions from one
 * state to another as the result of an event and which will throw an exception when asked to make a
 * transition for which there is no mapping
 *
 * @param <S> the type to use as the state
 * @param <E> the type to use as the event
 */
public interface StateTransitionManager<S, E> {

  /**
   * execute a transition
   *
   * @param sourceState the starting state
   * @param event the event to apply to the source state
   * @return the destination state - it is the responsibility of the caller to persist this
   * @throws CTPException an illegal event was applied to the source state
   */
  S transition(S sourceState, E event) throws CTPException;

  /**
   * @param sourceState the initial state it is in
   * @return the available states that an initial state can move to and the events required to get
   *     there
   */
  Map<E, S> getAvailableTransitions(S sourceState);
}
