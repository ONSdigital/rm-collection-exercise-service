SET schema 'collectionexercise';

INSERT INTO   state(statePK) VALUES('INIT');
INSERT INTO   state(statePK) VALUES('PENDING');
INSERT INTO   state(statePK) VALUES('EXECUTED');
INSERT INTO   state(statePK) VALUES('VALIDATED');
INSERT INTO   state(statePK) VALUES('PUBLISHED');
INSERT INTO   state(statePK) VALUES('FAILEDVALIDATION');

INSERT INTO   sampleunittype(sampleunittypePK) VALUES('B');
INSERT INTO   sampleunittype(sampleunittypePK) VALUES('BI');

INSERT INTO survey(id, surveyPK, surveyref) VALUES('99f6cd6d-880c-4b36-b157-aeda409ec441',1,'221'); 

INSERT INTO collectionexercise(id,surveyFK,exercisePK,name,scheduledstartdatetime,scheduledexecutiondatetime,scheduledreturndatetime,scheduledenddatetime,periodstartdatetime,stateFK)
VALUES ('14fb3e68-4dca-46db-bf49-04b84e07e77c',1,1,'BRES_2016','2017-09-01','2017-08-24','2017-10-07','2018-03-31','2017-09-09','INIT');

INSERT INTO casetypedefault (casetypedefaultPK,surveyFK,sampleunittypeFK,actionplanId) VALUES (1,1,'B' ,'e71002ac-3575-47eb-b87f-cd9db92bf9a7');
INSERT INTO casetypedefault (casetypedefaultPK,surveyFK,sampleunittypeFK,actionplanId) VALUES (2,1,'BI','0009e978-0932-463b-a2a1-b45cb3ffcb2a');
