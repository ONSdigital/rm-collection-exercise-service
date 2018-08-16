package uk.gov.ons.ctp.response.collection.exercise.service.impl.actionrule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.representation.ActionRuleDTO;
import uk.gov.ons.ctp.response.action.representation.ActionType;

@RunWith(MockitoJUnitRunner.class)
public class ActionRulesFilterTest {

  private static final String MULTIPLE_ACTION_RULES_FOUND_EXCEPTION =
      "Multiple BSNE Action Rules Found for Action Plan %s, remove all but 1 before continuing";
  private static final String NO_BSNE_ACTION_RULES_FOUND_EXCEPTION =
      "No BSNE Action Rules Found for Action Plan %s, please create one instead";
  private static final UUID ACTION_PLAN_ID = UUID.randomUUID();
  @Rule public ExpectedException thrown = ExpectedException.none();

  @InjectMocks private ActionRulesFilter actionRulesFilter;

  @Test
  public void raiseCTPExceptionIfNoBSNEActionRulesFound() throws CTPException {
    thrown.expect(CTPException.class);
    thrown.expectMessage(
        String.format(NO_BSNE_ACTION_RULES_FOUND_EXCEPTION, ACTION_PLAN_ID.toString()));

    final ActionRuleDTO actionRuleDTO = new ActionRuleDTO();
    actionRuleDTO.setActionTypeName(ActionType.BSNL);
    final List<ActionRuleDTO> actionRuleDTOs = Collections.singletonList(actionRuleDTO);

    actionRulesFilter.getActionRuleByType(actionRuleDTOs, ActionType.BSNE, ACTION_PLAN_ID);
  }

  @Test
  public void raiseCTPExceptionIfMultipleBSNEActionRulesFound() throws CTPException {
    thrown.expect(CTPException.class);
    thrown.expectMessage(
        String.format(MULTIPLE_ACTION_RULES_FOUND_EXCEPTION, ACTION_PLAN_ID.toString()));

    final ActionRuleDTO actionRuleDTO1 = new ActionRuleDTO();
    actionRuleDTO1.setActionTypeName(ActionType.BSNE);
    final ActionRuleDTO actionRuleDTO2 = new ActionRuleDTO();
    actionRuleDTO2.setActionTypeName(ActionType.BSNE);
    final List<ActionRuleDTO> actionRuleDTOs = Arrays.asList(actionRuleDTO1, actionRuleDTO2);

    actionRulesFilter.getActionRuleByType(actionRuleDTOs, ActionType.BSNE, ACTION_PLAN_ID);
  }

  @Test
  public void returnActionRuleByTypeBSNE() throws CTPException {
    final ActionRuleDTO actionRuleDTO1 = new ActionRuleDTO();
    actionRuleDTO1.setActionTypeName(ActionType.BSNE);
    final ActionRuleDTO actionRuleDTO2 = new ActionRuleDTO();
    actionRuleDTO2.setActionTypeName(ActionType.BSNL);
    final List<ActionRuleDTO> actionRuleDTOs = Arrays.asList(actionRuleDTO1, actionRuleDTO2);

    ActionRuleDTO returnedActionRule =
        actionRulesFilter.getActionRuleByType(actionRuleDTOs, ActionType.BSNE, ACTION_PLAN_ID);

    assertThat(returnedActionRule, is(actionRuleDTO1));
  }
}
