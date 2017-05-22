SET schema 'collectionexercise';

INSERT INTO survey(surveyid,surveyref) VALUES('cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87','BRES_2016');
INSERT INTO survey(surveyid,surveyref) VALUES('cb0711c3-0ac8-41d3-ae0e-567e5ea1ef88','BRES_2017');

INSERT INTO collectionexercise(surveyid,exerciseid,scheduledstartdatetime,scheduledexecutiondatetime,scheduledreturndatetime,scheduledenddatetime,scheduledsurveydate,state)
VALUES ('cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87','c6467711-21eb-4e78-804c-1db8392f93fb','2017-09-01','2017-08-24','2017-10-07','2018-03-31','2017-09-09','INIT'),
('cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87','c6467711-21eb-4e78-804c-1db8392f93fc','2017-09-01','2017-08-24','2017-10-07','2018-03-31','2017-09-09','INIT'),
('cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87','c6467711-21eb-4e78-804c-1db8392f93fd','2017-09-01','2017-08-24','2017-10-07','2018-03-31','2017-09-09','INIT'),
('cb0711c3-0ac8-41d3-ae0e-567e5ea1ef88','c6467711-21eb-4e78-804c-1db8392f93fe','2017-09-01','2017-08-24','2017-10-07','2018-03-31','2017-09-09','INIT');

INSERT INTO casetypedefault (casetypedefaultid,surveyid,sampleunittype,actionplanid) VALUES (1,'cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87','B',1);
INSERT INTO casetypedefault (casetypedefaultid,surveyid,sampleunittype,actionplanid) VALUES (2,'cb0711c3-0ac8-41d3-ae0e-567e5ea1ef88','BI',2);