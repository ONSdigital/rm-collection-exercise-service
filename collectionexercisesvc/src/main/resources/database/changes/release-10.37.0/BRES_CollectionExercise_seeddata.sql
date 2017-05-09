SET schema 'collectionexercise';

INSERT INTO collectionexercise(exerciseid,scheduledstartdatetime,scheduledexecutiondatetime,scheduledreturndatetime,scheduledenddatetime,scheduledsurveydate,state)
VALUES (1,'2017-09-01','2017-08-24','2017-10-07','2018-03-31','2017-09-09','PENDING');

INSERT INTO survey(surveyid,name) VALUES(1,'BRES_2016'); 

INSERT INTO casetypedefault (casetypedefaultid,surveyid,sampleunittype,actionplanid) VALUES (1,1,'B',1);
INSERT INTO casetypedefault (casetypedefaultid,surveyid,sampleunittype,actionplanid) VALUES (2,1,'BI',2);
