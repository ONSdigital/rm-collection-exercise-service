SET schema 'collectionexercise';

INSERT INTO survey(id, surveyid, surveyref) VALUES('99f6cd6d-880c-4b36-b157-aeda409ec441',1,'BRES_2016'); 

INSERT INTO collectionexercise(id,surveyid,exerciseid,name,scheduledstartdatetime,scheduledexecutiondatetime,scheduledreturndatetime,scheduledenddatetime,periodstartdatetime,state)
VALUES ('14fb3e68-4dca-46db-bf49-04b84e07e77c',1,1,'201601','2017-09-01','2017-08-24','2017-10-07','2018-03-31','2017-09-09','INIT');

INSERT INTO casetypedefault (casetypedefaultid,surveyid,sampleunittype,actionplanid) VALUES (1,1,'B',1);
INSERT INTO casetypedefault (casetypedefaultid,surveyid,sampleunittype,actionplanid) VALUES (2,1,'BI',2);


