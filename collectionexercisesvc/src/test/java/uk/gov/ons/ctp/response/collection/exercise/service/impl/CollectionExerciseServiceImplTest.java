package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;

import java.util.*;

/**
 * UnitTests for CollectionExerciseServiceImpl
 */

@Slf4j
public class CollectionExerciseServiceImplTest {

  private static final UUID ACTIONPLANID1 = UUID.fromString("60df56d9-f491-4ac8-b256-a10154290a8b");
  private static final UUID ACTIONPLANID2 = UUID.fromString("60df56d9-f491-4ac8-b256-a10154290a8c");
  private static final UUID ACTIONPLANID3 = UUID.fromString("70df56d9-f491-4ac8-b256-a10154290a8b");
  private static final UUID ACTIONPLANID4 = UUID.fromString("80df56d9-f491-4ac8-b256-a10154290a8b");

  @Test
  public void givenThatDefaultAndOverrideAreEmptyExpectEmpty() {
    List<CaseTypeDefault> caseTypeDefaultList = new ArrayList<>();
    List<CaseTypeOverride> caseTypeOverrideList = new ArrayList<>();

    CollectionExerciseServiceImpl collectionExerciseServiceImpl = new CollectionExerciseServiceImpl();

    Collection<CaseType> caseTypeList = collectionExerciseServiceImpl.createCaseTypeList(caseTypeDefaultList, caseTypeOverrideList);

    Assert.assertTrue(caseTypeList.isEmpty());

  }

  @Test
  public void givenThatDefaultIsPopulatedAndOverrideIsEmptyExpectDefaultOnly() {
    List<CaseTypeDefault> caseTypeDefaultList = new ArrayList<>();
    List<CaseTypeOverride> caseTypeOverrideList = new ArrayList<>();

    caseTypeDefaultList.add(createCaseTypeDefault(ACTIONPLANID1, "B"));

    CollectionExerciseServiceImpl collectionExerciseServiceImpl = new CollectionExerciseServiceImpl();

    Collection<CaseType> caseTypeList = collectionExerciseServiceImpl.createCaseTypeList(caseTypeDefaultList, caseTypeOverrideList);

    Assert.assertTrue(caseTypeList.iterator().next().getActionPlanId().equals(ACTIONPLANID1));
  }

  @Test
  public void givenThatDefaultIsEmptyAndOverrideIsPopulatedExpectEmpty() {
    List<CaseTypeDefault> caseTypeDefaultList = new ArrayList<>();
    List<CaseTypeOverride> caseTypeOverrideList = new ArrayList<>();

    caseTypeOverrideList.add(createCaseTypeOverride(ACTIONPLANID3, "B"));

    CollectionExerciseServiceImpl collectionExerciseServiceImpl = new CollectionExerciseServiceImpl();

    Collection<CaseType> caseTypeList = collectionExerciseServiceImpl.createCaseTypeList(caseTypeDefaultList, caseTypeOverrideList);

    Assert.assertTrue(caseTypeList.iterator().next().getActionPlanId().equals(ACTIONPLANID3));
  }

  @Test
  public void givenThatDefaultIsPopulatedAndOverrideIsPopulatedExpectOnlyOverride() {
    List<CaseTypeDefault> caseTypeDefaultList = new ArrayList<>();
    List<CaseTypeOverride> caseTypeOverrideList = new ArrayList<>();

    caseTypeDefaultList.add(createCaseTypeDefault(ACTIONPLANID1, "B"));

    caseTypeOverrideList.add(createCaseTypeOverride(ACTIONPLANID3, "B"));

    CollectionExerciseServiceImpl collectionExerciseServiceImpl = new CollectionExerciseServiceImpl();

    Collection<CaseType> caseTypeList = collectionExerciseServiceImpl.createCaseTypeList(caseTypeDefaultList, caseTypeOverrideList);

    Assert.assertTrue(caseTypeList.iterator().next().getActionPlanId().equals(ACTIONPLANID3));

  }

  @Test
  public void givenThatDefaultIsPopulatedWithMultipleAndOverrideIsPopulatedWithOneExpectOneDefaultAndOneOverride() {

    List<CaseTypeDefault> caseTypeDefaultList = new ArrayList<>();
    List<CaseTypeOverride> caseTypeOverrideList = new ArrayList<>();

    caseTypeDefaultList.add(createCaseTypeDefault(ACTIONPLANID1, "B"));
    caseTypeDefaultList.add(createCaseTypeDefault(ACTIONPLANID2, "BI"));

    caseTypeOverrideList.add(createCaseTypeOverride(ACTIONPLANID3, "BI"));

    CollectionExerciseServiceImpl collectionExerciseServiceImpl = new CollectionExerciseServiceImpl();

    Collection<CaseType> caseTypeList = collectionExerciseServiceImpl.createCaseTypeList(caseTypeDefaultList, caseTypeOverrideList);
    Iterator<CaseType> i = caseTypeList.iterator();
    Assert.assertTrue(i.next().getActionPlanId().equals(ACTIONPLANID1));
    Assert.assertTrue(i.next().getActionPlanId().equals(ACTIONPLANID3));

  }

  @Test
  public void givenThatDefaultIsPopulatedWithMultipleAndOverrideIsPopulatedWithMultipleExpectMultipleOverride() {

    List<CaseTypeDefault> caseTypeDefaultList = new ArrayList<>();
    List<CaseTypeOverride> caseTypeOverrideList = new ArrayList<>();

    caseTypeDefaultList.add(createCaseTypeDefault(ACTIONPLANID1, "B"));
    caseTypeDefaultList.add(createCaseTypeDefault(ACTIONPLANID2, "BI"));

    caseTypeOverrideList.add(createCaseTypeOverride(ACTIONPLANID3, "B"));
    caseTypeOverrideList.add(createCaseTypeOverride(ACTIONPLANID4, "BI"));

    CollectionExerciseServiceImpl collectionExerciseServiceImpl = new CollectionExerciseServiceImpl();

    Collection<CaseType> caseTypeList = collectionExerciseServiceImpl.createCaseTypeList(caseTypeDefaultList, caseTypeOverrideList);

    Iterator<CaseType> i = caseTypeList.iterator();
    Assert.assertTrue(i.next().getActionPlanId().equals(ACTIONPLANID3));
    Assert.assertTrue(i.next().getActionPlanId().equals(ACTIONPLANID4));

  }

  private CaseTypeDefault createCaseTypeDefault(UUID actionPlanId, String sampleUnitTypeFK) {
    CaseTypeDefault.builder().caseTypeDefaultPK(1).surveyFK(1);
    return CaseTypeDefault.builder().actionPlanId(actionPlanId).sampleUnitTypeFK(sampleUnitTypeFK).build();
  }

  private CaseTypeOverride createCaseTypeOverride(UUID actionPlanId, String sampleUnitTypeFK) {
    CaseTypeOverride.builder().caseTypeOverridePK(1).exerciseFK(1);
    return CaseTypeOverride.builder().actionPlanId(actionPlanId).sampleUnitTypeFK(sampleUnitTypeFK).build();
  }

}
