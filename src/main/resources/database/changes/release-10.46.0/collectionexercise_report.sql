--create sequence for message logging
CREATE SEQUENCE collectionexercise.messagelogseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;

--create messagelog table
CREATE TABLE collectionexercise.messagelog
(
  messagelogpk bigint NOT NULL DEFAULT nextval('collectionexercise.messagelogseq'::regclass),
  messagetext character varying,
  jobid numeric,
  messagelevel character varying,
  functionname character varying,
  createddatetime timestamp with time zone,
  CONSTRAINT messagelogpk_pkey PRIMARY KEY (messagelogpk)
);


-- Sequence: collectionexercise.reportPKseq
-- DROP SEQUENCE collectionexercise.reportPKseq;

CREATE SEQUENCE collectionexercise.reportPKseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;


-- Table: collectionexercise.reporttype
-- DROP TABLE collectionexercise.reporttype;

CREATE TABLE collectionexercise.reporttype
(
    reporttypePK  character varying (20),
    displayorder  integer,
    displayname   character varying(40),
    CONSTRAINT reporttype_pkey PRIMARY KEY (reporttypePK)
);


-- Table: collectionexercise.report
-- DROP TABLE collectionexercise.report;

CREATE TABLE collectionexercise.report
(
    id             uuid NOT NULL,
    reportPK       bigint NOT NULL,
    reporttypeFK   character varying (20),
    contents       text ,
    createddatetime timestamp with time zone,
    CONSTRAINT report_pkey PRIMARY KEY (reportpk),
    CONSTRAINT report_uuid_key UNIQUE (id),
    CONSTRAINT reporttypefk_fkey FOREIGN KEY (reporttypefk)
    REFERENCES collectionexercise.reporttype (reporttypepk) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- Function: collectionexercise.logmessage(text, numeric, text, text)

-- DROP FUNCTION collectionexercise.logmessage(text, numeric, text, text);

CREATE OR REPLACE FUNCTION collectionexercise.logmessage(p_messagetext text DEFAULT NULL::text, p_jobid numeric DEFAULT NULL::numeric, p_messagelevel text DEFAULT NULL::text, p_functionname text DEFAULT NULL::text)
  RETURNS boolean AS
$BODY$
DECLARE
v_text TEXT ;
v_function TEXT;
BEGIN
INSERT INTO collectionexercise.messagelog
(messagetext, jobid, messagelevel, functionname, createddatetime )
values (p_messagetext, p_jobid, p_messagelevel, p_functionname, current_timestamp);
  RETURN TRUE;
EXCEPTION
WHEN OTHERS THEN
RETURN FALSE;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


-- Insert reports into report tables 
INSERT INTO collectionexercise.reporttype(reporttypePK,displayorder,displayname) VALUES('COLLECTIONEXERCISE',30,'Collection Exercise');

-- Function: collectionexercise.generate_collectionexercise_mi()

-- DROP FUNCTION collectionexercise.generate_collectionexercise_mi();

CREATE OR REPLACE FUNCTION collectionexercise.generate_collectionexercise_mi()
  RETURNS boolean AS
$BODY$
DECLARE

v_contents      text;
r_dataline      record;
v_rows          integer;

BEGIN
    
    PERFORM collectionexercise.logmessage(p_messagetext := 'GENERATING COLLECTION EXERCISE MI REPORT'
                              ,p_jobid := 0
                              ,p_messagelevel := 'INFO'
                              ,p_functionname := 'collectionexercise.generate_collectionexercise_mi');  
    
       v_rows     := 0;
       v_contents := '';
       v_contents := 'CE Name,Scheduled Start DateTime,Scheduled Execution DateTime,Scheduled Return DateTime,Scheduled End DateTime,Period Start DateTime,Period End DateTime,Actual Execution DateTime,Actual Publish DateTime,Executed By,State,Sample Size';

-- collectionexercise Report

       FOR r_dataline IN (SELECT  c.name
                                , c.scheduledstartdatetime
                                , c.scheduledexecutiondatetime
                                , c.scheduledreturndatetime
                                , c.scheduledenddatetime
                                , c.periodstartdatetime
                                , c.periodenddatetime
                                , c.actualexecutiondatetime
                                , c.actualpublishdatetime
                                , c.executedby
                                , c.stateFK
                                , c.samplesize 
                          FROM collectionexercise.collectionexercise c) LOOP

                                v_contents := v_contents                                    || chr(10) 
                                || r_dataline.name                                          || ','
                                || COALESCE(r_dataline.scheduledstartdatetime::text,'')     || ','
                                || COALESCE(r_dataline.scheduledexecutiondatetime::text,'') || ','
                                || COALESCE(r_dataline.scheduledreturndatetime::text,'')    || ','
                                || COALESCE(r_dataline.scheduledenddatetime::text,'')       || ','
                                || COALESCE(r_dataline.periodstartdatetime::text,'')        || ','
                                || COALESCE(r_dataline.periodenddatetime::text,'')          || ','
                                || COALESCE(r_dataline.actualexecutiondatetime::text,'')    || ','
                                || COALESCE(r_dataline.actualpublishdatetime ::text,'')     || ','
                                || COALESCE(r_dataline.executedby::text,'')                 || ','
                                || COALESCE(r_dataline.stateFK::text,'')                    || ','
                                || COALESCE(r_dataline.samplesize::text,'');
                              
             v_rows := v_rows+1;  
       END LOOP;       

       -- Insert the data into the report table
       INSERT INTO collectionexercise.report (id, reportPK,reporttypeFK,contents, createddatetime) VALUES(gen_random_uuid(), nextval('collectionexercise.reportPKseq'), 'COLLECTIONEXERCISE', v_contents, CURRENT_TIMESTAMP); 

 
       PERFORM collectionexercise.logmessage(p_messagetext := 'GENERATING COLLECTION EXERCISE MI REPORT COMPLETED ROWS WRIITEN = ' || v_rows
                                        ,p_jobid := 0
                                        ,p_messagelevel := 'INFO'
                                        ,p_functionname := 'collectionexercise.generate_collectionexercise_mi'); 
      
    
       PERFORM collectionexercise.logmessage(p_messagetext := 'COLLECTION EXERCISE MI REPORT GENERATED'
                                        ,p_jobid := 0
                                        ,p_messagelevel := 'INFO'
                                        ,p_functionname := 'collectionexercise.generate_collectionexercise_mi');
  
  RETURN TRUE;

  EXCEPTION
  WHEN OTHERS THEN   
     PERFORM collectionexercise.logmessage(p_messagetext := 'GENERATE REPORTS EXCEPTION TRIGGERED SQLERRM: ' || SQLERRM || ' SQLSTATE : ' || SQLSTATE
                               ,p_jobid := 0
                               ,p_messagelevel := 'FATAL'
                               ,p_functionname := 'collectionexercise.generate_collectionexercise_mi');
                               
  RETURN FALSE;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE SECURITY DEFINER
  COST 100;
