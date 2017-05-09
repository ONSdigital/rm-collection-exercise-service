set schema 'collectionexercise';

-- Create Sequences
CREATE SEQUENCE messageseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;


-- Table: messagelog
CREATE TABLE messagelog 
(
    messageid       bigint DEFAULT nextval('messageseq'::regclass) NOT NULL,
    messagetext     character varying,
    jobid           numeric,
    messagelevel    character varying,
    functionname    character varying,
    createddatetime timestamp with time zone
);


-- Sequence:   sampleunitgroupidseq
-- DROP SEQUENCE   sampleunitgroupidseq;
CREATE SEQUENCE sampleunitgroupidseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;


-- Sequence:   exerciseidseq
-- DROP SEQUENCE   exerciseidseq;
CREATE SEQUENCE exerciseidseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;



-- Table: survey
CREATE TABLE survey
(
  surveyid integer NOT NULL,
  name character varying(100) NOT NULL
);

ALTER TABLE ONLY survey ADD CONSTRAINT surveyid_pkey PRIMARY KEY (surveyid);



-- Table: state
CREATE TABLE state
(
  state character varying(20) NOT NULL
);

ALTER TABLE ONLY state ADD CONSTRAINT state_pkey PRIMARY KEY (state);

INSERT INTO   state(state) VALUES('INIT');
INSERT INTO   state(state) VALUES('PENDING');
INSERT INTO   state(state) VALUES('EXECUTED');
INSERT INTO   state(state) VALUES('VALIDATED');
INSERT INTO   state(state) VALUES('PUBLISHED');
INSERT INTO   state(state) VALUES('FAILEDVALIDATION');


-- Table: sampleunittype
CREATE TABLE sampleunittype
(
sampleunittype character varying(2) NOT NULL-- example 'H','C','B' 'BI'
);

ALTER TABLE ONLY sampleunittype ADD CONSTRAINT sampleunittype_pkey PRIMARY KEY (sampleunittype);

INSERT INTO   sampleunittype(sampleunittype) VALUES('B');
INSERT INTO   sampleunittype(sampleunittype) VALUES('BI');



-- Table: CaseTypeDefault
CREATE TABLE CaseTypeDefault
(
casetypedefaultid      integer NOT NULL,
surveyid               integer NOT NULL,
sampleunittype         character varying(2) NOT NULL, -- example 'H','C','B' 'BI'
actionplanid           integer NOT NULL
);

ALTER TABLE ONLY CaseTypeDefault ADD CONSTRAINT casetypedefaultid_pkey PRIMARY KEY (casetypedefaultid);
ALTER TABLE ONLY CaseTypeDefault ADD CONSTRAINT surveyid_fkey          FOREIGN KEY (surveyid)       REFERENCES survey(surveyid);
ALTER TABLE ONLY CaseTypeDefault ADD CONSTRAINT sampleunittype_fkey    FOREIGN KEY (sampleunittype) REFERENCES sampleunittype(sampleunittype);



-- Table: CollectionExercise
CREATE TABLE CollectionExercise
(
exerciseid                 bigint NOT NULL DEFAULT nextval('exerciseidseq'::regclass),
surveyid                   integer NOT NULL,
scheduledstartdatetime     timestamp with time zone,
scheduledexecutiondatetime timestamp with time zone,
scheduledreturndatetime    timestamp with time zone,
scheduledenddatetime       timestamp with time zone,
scheduledsurveydate        timestamp with time zone,
actualexecutiondatetime    timestamp with time zone,
actualpublishdatetime      timestamp with time zone,
executedby                 character varying(50),
state                      character varying(20) NOT NULL
);

ALTER TABLE ONLY   CollectionExercise ADD CONSTRAINT exerciseid_pkey PRIMARY KEY (exerciseid);
ALTER TABLE ONLY   CollectionExercise ADD CONSTRAINT surveyid_fkey   FOREIGN KEY (surveyid) REFERENCES survey(surveyid);


-- Table: CaseTypeOverride
CREATE TABLE CaseTypeOverride
(
casetypeoverrideid     integer NOT NULL,
exerciseid             bigint NOT NULL,
sampleunittype         character varying(2) NOT NULL, -- example 'H','C','B' 'BI'
actionplanid           integer NOT NULL
);

ALTER TABLE ONLY   CaseTypeOverride ADD CONSTRAINT casetypeoverrideid_pkey PRIMARY KEY (casetypeoverrideid);
ALTER TABLE ONLY   CaseTypeOverride ADD CONSTRAINT exerciseid_fkey         FOREIGN KEY (exerciseid) REFERENCES collectionexercise(exerciseid);



-- Table: SampleUnitGroup
CREATE TABLE SampleUnitGroup
(
sampleunitgroupid     bigint NOT NULL DEFAULT nextval('sampleunitgroupidseq'::regclass),
exerciseid            bigint NOT NULL,
formtype              character varying(10) NOT NULL, -- census questionset
state                 character varying(20) NOT NULL,
createddatetime       timestamp with time zone,
modifieddatetime      timestamp with time zone
);

ALTER TABLE ONLY   SampleUnitGroup ADD CONSTRAINT sampleunitgroupid_pkey PRIMARY KEY (sampleunitgroupid);
ALTER TABLE ONLY   SampleUnitGroup ADD CONSTRAINT exerciseid_fkey        FOREIGN KEY (exerciseid) REFERENCES collectionexercise(exerciseid);
ALTER TABLE ONLY   SampleUnitGroup ADD CONSTRAINT state_fkey             FOREIGN KEY (state)      REFERENCES state(state);



-- Table: SampleUnit
CREATE TABLE SampleUnit
(
sampleunitid        bigint NOT NULL,
sampleunitgroupid   bigint NOT NULL, 
sampleunitref       character varying(20) NOT NULL,
sampleunittype      character varying(2) NOT NULL -- example 'H','C','B' 'BI'
);

ALTER TABLE ONLY   SampleUnit ADD CONSTRAINT sampleunitid_pkey       PRIMARY KEY (sampleunitid);
ALTER TABLE ONLY   SampleUnit ADD CONSTRAINT sampleunitgroupid_fkey  FOREIGN KEY (sampleunitgroupid) REFERENCES SampleUnitGroup(sampleunitgroupid);
ALTER TABLE ONLY   SampleUnit ADD CONSTRAINT sampleunittype_fkey     FOREIGN KEY (sampleunittype)    REFERENCES sampleunittype(sampleunittype);



