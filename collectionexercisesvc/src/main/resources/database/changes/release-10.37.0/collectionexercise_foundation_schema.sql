Set schema 'collectionexercise';

-- Sequence: sampleunitgroupPKdseq
-- DROP SEQUENCE sampleunitgroupPKseq;
CREATE SEQUENCE sampleunitgroupPKseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;


-- Sequence: exercisePKseq
-- DROP SEQUENCE exercisePKseq;
CREATE SEQUENCE exercisePKseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;



-- Table: survey
CREATE TABLE survey
( id        uuid NOT NULL,
  surveyPK  integer NOT NULL,
  surveyref character varying(100) NOT NULL
);


ALTER TABLE ONLY survey ADD CONSTRAINT surveyPK_pkey PRIMARY KEY (surveyPK);

-- Add index
ALTER TABLE survey ADD CONSTRAINT survey_id_uuid_key UNIQUE (id);



-- Table: state
CREATE TABLE state
(
  statePK character varying(20) NOT NULL
);

ALTER TABLE ONLY state ADD CONSTRAINT statePK_pkey PRIMARY KEY (statePK);

-- Table: sampleunittype
CREATE TABLE sampleunittype
(
sampleunittypePK character varying(2) NOT NULL-- example 'H','C','B' 'BI'
);

ALTER TABLE ONLY sampleunittype ADD CONSTRAINT sampleunittypePK_pkey PRIMARY KEY (sampleunittypePK);


-- Table: CaseTypeDefault
CREATE TABLE CaseTypeDefault
(
casetypedefaultPK      integer NOT NULL,
surveyFK               integer NOT NULL,
sampleunittypeFK       character varying(2) NOT NULL, -- example 'H','C','B' 'BI'
actionplanId           uuid
);

ALTER TABLE ONLY CaseTypeDefault ADD CONSTRAINT casetypedefaultPK_pkey PRIMARY KEY (casetypedefaultPK);
ALTER TABLE ONLY CaseTypeDefault ADD CONSTRAINT ctd_surveyFK_fkey      FOREIGN KEY (surveyFK)         REFERENCES survey(surveyPK);
ALTER TABLE ONLY CaseTypeDefault ADD CONSTRAINT sampleunittype_fkey    FOREIGN KEY (sampleunittypeFK) REFERENCES sampleunittype(sampleunittypePK);



-- Table: CollectionExercise
CREATE TABLE CollectionExercise
(
id                         uuid    NOT NULL,
exercisePK                 bigint  NOT NULL DEFAULT nextval('exercisePKseq'::regclass),
surveyFK                   integer NOT NULL,
name                       character varying(20),
scheduledstartdatetime     timestamp with time zone,
scheduledexecutiondatetime timestamp with time zone,
scheduledreturndatetime    timestamp with time zone,
scheduledenddatetime       timestamp with time zone,
periodstartdatetime        timestamp with time zone,
periodenddatetime          timestamp with time zone,
actualexecutiondatetime    timestamp with time zone,
actualpublishdatetime      timestamp with time zone,
executedby                 character varying(50),
stateFK                    character varying(20) NOT NULL,
samplesize	           integer
);

ALTER TABLE ONLY   CollectionExercise ADD CONSTRAINT exercisePK_pkey  PRIMARY KEY (exercisePK);
ALTER TABLE ONLY   CollectionExercise ADD CONSTRAINT surveyFK_fkey    FOREIGN KEY (surveyFK) REFERENCES survey(surveyPK);
ALTER TABLE ONLY   CollectionExercise ADD CONSTRAINT stateFK_fkey     FOREIGN KEY (stateFK)  REFERENCES state(statePK);

-- Add index
ALTER TABLE collectionexercise ADD CONSTRAINT ce_id_uuid_key UNIQUE (id);



-- Table: CaseTypeOverride
CREATE TABLE CaseTypeOverride
(
casetypeoverridePK     integer NOT NULL,
exerciseFK             bigint NOT NULL,
sampleunittypeFK       character varying(2) NOT NULL, -- example 'H','C','B' 'BI'
actionplanId           uuid
);

ALTER TABLE ONLY   CaseTypeOverride ADD CONSTRAINT casetypeoverridePK_pkey PRIMARY KEY (casetypeoverridePK);
ALTER TABLE ONLY   CaseTypeOverride ADD CONSTRAINT exerciseFK_fkey         FOREIGN KEY (exerciseFK) REFERENCES collectionexercise(exercisePK);
ALTER TABLE ONLY   CaseTypeOverride ADD CONSTRAINT sampleunittypeFK_fkey         FOREIGN KEY (sampleunittypeFK) REFERENCES sampleunittype(sampleunittypePK);



-- Table: SampleUnitGroup
CREATE TABLE SampleUnitGroup
(
sampleunitgroupPK     bigint NOT NULL DEFAULT nextval('sampleunitgroupPKseq'::regclass),
exerciseFK            bigint NOT NULL,
formtype              character varying(10) NOT NULL, -- census questionset
stateFK               character varying(20) NOT NULL,
createddatetime       timestamp with time zone,
modifieddatetime      timestamp with time zone
);

ALTER TABLE ONLY   SampleUnitGroup ADD CONSTRAINT sampleunitgroupPK_pkey PRIMARY KEY (sampleunitgroupPK);
ALTER TABLE ONLY   SampleUnitGroup ADD CONSTRAINT exerciseFK_fkey        FOREIGN KEY (exerciseFK) REFERENCES collectionexercise(exercisePK);
ALTER TABLE ONLY   SampleUnitGroup ADD CONSTRAINT stateFK_fkey           FOREIGN KEY (stateFK)    REFERENCES state(statePK);



-- Table: SampleUnit
CREATE TABLE SampleUnit
(
sampleunitPK            bigint  NOT NULL,
sampleunitgroupFK       bigint  NOT NULL, 
collectioninstrumentId  uuid,
partyId                 uuid,
sampleunitref           character varying(20) NOT NULL,
sampleunittypeFK        character varying(2) NOT NULL -- example 'H','C','B' 'BI'
);

ALTER TABLE ONLY   SampleUnit ADD CONSTRAINT sampleunitPK_pkey       PRIMARY KEY (sampleunitPK);
ALTER TABLE ONLY   SampleUnit ADD CONSTRAINT sampleunitgroupFK_fkey  FOREIGN KEY (sampleunitgroupFK) REFERENCES SampleUnitGroup(sampleunitgroupPK);
ALTER TABLE ONLY   SampleUnit ADD CONSTRAINT sampleunittypeFK_fkey   FOREIGN KEY (sampleunittypeFK)  REFERENCES sampleunittype(sampleunittypePK);



