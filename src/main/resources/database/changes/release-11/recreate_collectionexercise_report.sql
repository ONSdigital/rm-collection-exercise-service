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

       FOR r_dataline IN (SELECT  c.scheduledstartdatetime
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
