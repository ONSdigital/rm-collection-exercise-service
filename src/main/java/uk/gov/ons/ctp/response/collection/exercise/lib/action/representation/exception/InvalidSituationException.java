package uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.exception;

import uk.gov.ons.ctp.response.collection.exercise.lib.action.representation.Situation;

public class InvalidSituationException extends RuntimeException {

  private static final String TOO_LONG_MESSAGE =
      "Situation can have a maximum length of %d; got \"%s\"";

  private InvalidSituationException(String message) {
    super(message);
  }

  public static InvalidSituationException tooLong(String situation) {
    return new InvalidSituationException(
        String.format(TOO_LONG_MESSAGE, Situation.MAXIMUM_LENGTH, situation));
  }
}
