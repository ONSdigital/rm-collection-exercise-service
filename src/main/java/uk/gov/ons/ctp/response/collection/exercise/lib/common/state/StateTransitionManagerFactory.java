package uk.gov.ons.ctp.response.collection.exercise.lib.common.state;

/**
 * Simple interface to accomodate later refactoring - allows Case And Action services to refer to an
 * abstract notion of factory while using a temporarily concrete service specific impl
 */
public interface StateTransitionManagerFactory {

  /**
   * Get the StateTransitionManager for a given entity
   *
   * @param <S> the state type
   * @param <E> the event type
   * @param entity the entity identified by name
   * @return the StateTransitionManager for the given entity
   */
  <S, E> StateTransitionManager<S, E> getStateTransitionManager(String entity);
}
