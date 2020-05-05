package uk.gov.ons.ctp.response.collection.exercise.lib.action.representation;

import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.exception.InvalidSituationException;

public class Situation {
  public static final int MAXIMUM_LENGTH = 100;
  private final String situation;

  public Situation(String situation) {
    if (situation.length() > MAXIMUM_LENGTH) {
      throw InvalidSituationException.tooLong(situation);
    }

    this.situation = situation;
  }

  @Override
  public String toString() {
    return situation;
  }
}
