package uk.gov.ons.ctp.response.collection.exercise.state;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import uk.gov.ons.ctp.common.state.BasicStateTransitionManager;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.state.StateTransitionManagerFactory;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;

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
    managers.put(COLLLECTIONEXERCISE_ENTITY, sampleUnitGroupStateTransitionManager);

  }

  /**
   * Create and initalise the factory with the concrete StateTransitionManager
   * for the CollectionExercise entity
   *
   * @return StateTransitionManager
   */
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
      createCollectionExerciseStateTransitionManager() {

    Map<CollectionExerciseState, Map<CollectionExerciseEvent, CollectionExerciseState>> transitions = new HashMap<>();

    // INIT
    Map<CollectionExerciseEvent, CollectionExerciseState> transitionForInit =
        new HashMap<CollectionExerciseEvent, CollectionExerciseState>();
    transitionForInit.put(CollectionExerciseEvent.REQUEST, CollectionExerciseState.PENDING);
    transitions.put(CollectionExerciseState.INIT, transitionForInit);

    // PENDING
    Map<CollectionExerciseEvent, CollectionExerciseState> transitionForPending =
        new HashMap<CollectionExerciseEvent, CollectionExerciseState>();
    transitionForPending.put(CollectionExerciseEvent.EXECUTE, CollectionExerciseState.EXECUTED);
    transitions.put(CollectionExerciseState.PENDING, transitionForPending);

    // EXECUTED
    Map<CollectionExerciseEvent, CollectionExerciseState> transitionForExecuted =
        new HashMap<CollectionExerciseEvent, CollectionExerciseState>();
    transitionForExecuted.put(CollectionExerciseEvent.VALIDATE, CollectionExerciseState.VALIDATED);
    transitionForExecuted.put(CollectionExerciseEvent.INVALIDATED, CollectionExerciseState.FAILEDVALIDATION);
    transitions.put(CollectionExerciseState.EXECUTED, transitionForExecuted);

    // VALIDATED
    Map<CollectionExerciseEvent, CollectionExerciseState> transitionForValidated =
        new HashMap<CollectionExerciseEvent, CollectionExerciseState>();
    transitionForValidated.put(CollectionExerciseEvent.PUBLISH, CollectionExerciseState.PUBLISHED);
    transitions.put(CollectionExerciseState.VALIDATED, transitionForValidated);

    return new BasicStateTransitionManager<>(transitions);

  }

  /**
   * Create and initalise the factory with the concrete StateTransitionManager
   * for the SampleUnitGroup entity
   *
   * @return StateTransitionManager
   */
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent>
      createSampleUnitGroupStateTransitionManager() {

    Map<SampleUnitGroupState, Map<SampleUnitGroupEvent, SampleUnitGroupState>> transitions = new HashMap<>();

    // INIT
    Map<SampleUnitGroupEvent, SampleUnitGroupState> transitionForInit =
        new HashMap<SampleUnitGroupEvent, SampleUnitGroupState>();
    transitionForInit.put(SampleUnitGroupEvent.VALIDATE, SampleUnitGroupState.VALIDATED);
    transitions.put(SampleUnitGroupState.INIT, transitionForInit);

    // VALIDATED
    Map<SampleUnitGroupEvent, SampleUnitGroupState> transitionForValidated =
        new HashMap<SampleUnitGroupEvent, SampleUnitGroupState>();
    transitionForValidated.put(SampleUnitGroupEvent.PUBLISH, SampleUnitGroupState.PUBLISHED);
    transitions.put(SampleUnitGroupState.VALIDATED, transitionForValidated);

    return new BasicStateTransitionManager<>(transitions);
  }

  @Override
  public StateTransitionManager<?, ?> getStateTransitionManager(String entity) {
    return managers.get(entity);
  }

}
