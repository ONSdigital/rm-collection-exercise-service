package uk.gov.ons.ctp.response.collection.exercise.state;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import uk.gov.ons.ctp.common.state.BasicStateTransitionManager;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.state.StateTransitionManagerFactory;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;

/**
 * State transition manager factory for the collection exercise service.
 */
@Component
public class CollectionExerciseStateTransitionManagerFactory implements StateTransitionManagerFactory {

  public static final String COLLLECTIONEXERCISE_ENTITY = "CollectionExercise";

  public static final String SAMPLEUNITGROUP_ENTITY = "SampleUnitGroup";

  private Map<String, StateTransitionManager<?, ?>> managers;

  /**
   * Create and initialise the factory with concrete StateTransitionManagers for
   * each required entity
   */
  public CollectionExerciseStateTransitionManagerFactory() {
    managers = new HashMap<>();

    StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent> collectionExerciseStateTransitionManager =
        createCollectionExerciseStateTransitionManager();
    managers.put(COLLLECTIONEXERCISE_ENTITY, collectionExerciseStateTransitionManager);

    StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupStateTransitionManager =
        createSampleUnitGroupStateTransitionManager();
    managers.put(SAMPLEUNITGROUP_ENTITY, sampleUnitGroupStateTransitionManager);

  }

  /**
   * Create and initialise the factory with the concrete StateTransitionManager
   * for the CollectionExercise entity
   *
   * @return StateTransitionManager
   */
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
      createCollectionExerciseStateTransitionManager() {

    Map<CollectionExerciseState, Map<CollectionExerciseEvent, CollectionExerciseState>> transitions = new HashMap<>();

    // INIT
    Map<CollectionExerciseEvent, CollectionExerciseState> transitionForInit = new HashMap<>();
    transitionForInit.put(CollectionExerciseEvent.REQUEST, CollectionExerciseState.PENDING);
    transitions.put(CollectionExerciseState.INIT, transitionForInit);

    // PENDING
    Map<CollectionExerciseEvent, CollectionExerciseState> transitionForPending = new HashMap<>();
    transitionForPending.put(CollectionExerciseEvent.EXECUTE, CollectionExerciseState.EXECUTED);
    transitionForPending.put(CollectionExerciseEvent.REQUEST, CollectionExerciseState.PENDING);
    transitions.put(CollectionExerciseState.PENDING, transitionForPending);

    // EXECUTED
    Map<CollectionExerciseEvent, CollectionExerciseState> transitionForExecuted = new HashMap<>();
    transitionForExecuted.put(CollectionExerciseEvent.VALIDATE, CollectionExerciseState.VALIDATED);
    transitionForExecuted.put(CollectionExerciseEvent.INVALIDATED, CollectionExerciseState.FAILEDVALIDATION);
    transitions.put(CollectionExerciseState.EXECUTED, transitionForExecuted);

    // VALIDATED
    Map<CollectionExerciseEvent, CollectionExerciseState> transitionForValidated = new HashMap<>();
    transitionForValidated.put(CollectionExerciseEvent.PUBLISH, CollectionExerciseState.PUBLISHED);
    transitions.put(CollectionExerciseState.VALIDATED, transitionForValidated);

    StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent> collectionExerciseTransitionManager =
        new BasicStateTransitionManager<>(transitions);

    return collectionExerciseTransitionManager;

  }

  /**
   * Create and initialise the factory with the concrete StateTransitionManager
   * for the SampleUnitGroup entity
   *
   * @return StateTransitionManager
   */
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent>
      createSampleUnitGroupStateTransitionManager() {

    Map<SampleUnitGroupState, Map<SampleUnitGroupEvent, SampleUnitGroupState>> transitions = new HashMap<>();

    // INIT
    Map<SampleUnitGroupEvent, SampleUnitGroupState> transitionForInit = new HashMap<>();
    transitionForInit.put(SampleUnitGroupEvent.VALIDATE, SampleUnitGroupState.VALIDATED);
    transitions.put(SampleUnitGroupState.INIT, transitionForInit);

    // VALIDATED
    Map<SampleUnitGroupEvent, SampleUnitGroupState> transitionForValidated = new HashMap<>();
    transitionForValidated.put(SampleUnitGroupEvent.PUBLISH, SampleUnitGroupState.PUBLISHED);
    transitions.put(SampleUnitGroupState.VALIDATED, transitionForValidated);

    StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitTransitionManager =
        new BasicStateTransitionManager<>(transitions);

    return sampleUnitTransitionManager;
  }

  @SuppressWarnings("unchecked")
  @Override
  public StateTransitionManager<?, ?> getStateTransitionManager(String entity) {
    return managers.get(entity);
  }

}
