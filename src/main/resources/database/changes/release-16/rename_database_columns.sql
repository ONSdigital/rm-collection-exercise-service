ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN exercisepk TO exercise_pk;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN scheduledstartdatetime TO scheduled_start_date_time;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN scheduledexecutiondatetime TO scheduled_execution_date_time;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN scheduledreturndatetime TO scheduled_return_date_time;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN scheduledenddatetime TO scheduled_end_date_time;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN periodstartdatetime TO period_start_date_time;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN periodenddatetime TO period_end_date_time;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN actualexecutiondatetime TO actual_execution_date_time;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN actualpublishdatetime TO actual_publish_date_time;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN executedby TO executed_by;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN statefk TO state_fk;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN samplesize TO sample_size;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN exerciseref TO exercise_ref;
ALTER TABLE collectionexercise.collectionexercise RENAME COLUMN survey_uuid TO survey_id;

ALTER TABLE collectionexercise.casetypeoverride RENAME COLUMN casetypeoverridepk TO case_type_override_pk;
ALTER TABLE collectionexercise.casetypeoverride RENAME COLUMN exercisefk TO exercise_fk;
ALTER TABLE collectionexercise.casetypeoverride RENAME COLUMN sampleunittypefk TO sample_unit_type_fk;
ALTER TABLE collectionexercise.casetypeoverride RENAME COLUMN actionplanid TO action_plan_id;

ALTER TABLE collectionexercise.event RENAME COLUMN eventpk TO event_pk;
ALTER TABLE collectionexercise.event RENAME COLUMN collexfk TO exercise_fk;

ALTER TABLE collectionexercise.collectionexercisestate RENAME COLUMN statepk TO state_pk;

ALTER TABLE collectionexercise.sampleunit RENAME COLUMN sampleunitpk TO sample_unit_pk;
ALTER TABLE collectionexercise.sampleunit RENAME COLUMN sampleunitgroupfk TO sample_unit_group_fk;
ALTER TABLE collectionexercise.sampleunit RENAME COLUMN collectioninstrumentid TO collection+instrument_id;
ALTER TABLE collectionexercise.sampleunit RENAME COLUMN partyid TO party_id;
ALTER TABLE collectionexercise.sampleunit RENAME COLUMN sampleunitref TO sample_unit_ref;
ALTER TABLE collectionexercise.sampleunit RENAME COLUMN sampleunittypefk TO sample_unit_type_fk;

ALTER TABLE collectionexercise.sampleunitgroup RENAME COLUMN sampleunitgrouppk TO sample_unit_group_pk;
ALTER TABLE collectionexercise.sampleunitgroup RENAME COLUMN exercisefk TO exercise_fk;
ALTER TABLE collectionexercise.sampleunitgroup RENAME COLUMN formtype TO form_type;
ALTER TABLE collectionexercise.sampleunitgroup RENAME COLUMN statefk TO state_fk;
ALTER TABLE collectionexercise.sampleunitgroup RENAME COLUMN createddatetime TO created_date_time;
ALTER TABLE collectionexercise.sampleunitgroup RENAME COLUMN modifieddatetime TO modified_date_time;

ALTER TABLE collectionexercise.sampleunitgroupstate RENAME COLUMN statepk TO state_pk;

ALTER TABLE collectionexercise.casetypedefault RENAME COLUMN casetypedefaultpk TO case_type_default_pk;
ALTER TABLE collectionexercise.casetypedefault RENAME COLUMN sampleunittypepk TO sample_unit_type_pk;
ALTER TABLE collectionexercise.casetypedefault RENAME COLUMN actionplanid TO action_plan_id;
ALTER TABLE collectionexercise.casetypedefault RENAME COLUMN survey_uuid TO survey_id;

ALTER TABLE collectionexercise.sampleunittype RENAME COLUMN sampleunittypepk TO sample_unit_type_pk;

ALTER TABLE collectionexercise.samplelink RENAME COLUMN samplelinkpk TO sample_link_pk;
ALTER TABLE collectionexercise.samplelink RENAME COLUMN collectionexerciseid TO collection_exercise_id;
ALTER TABLE collectionexercise.samplelink RENAME COLUMN samplesummaryid TO sample_summary_id;