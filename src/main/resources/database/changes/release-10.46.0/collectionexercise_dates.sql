SET schema 'collectionexercise';

UPDATE collectionexercise.collectionexercise
SET 
  name = 'BRES_2017'
 ,scheduledexecutiondatetime = '2017-09-11 00:00:00+01' -- BST
 ,scheduledstartdatetime     = '2017-09-12 00:00:00+01' -- BST
 
 ,periodstartdatetime        = '2017-09-15 00:00:00+01' -- BST
 ,periodenddatetime          = '2017-09-15 23:59:59+01' -- BST

 ,scheduledreturndatetime    = '2017-10-06 00:00:00+01' -- BST
 ,scheduledenddatetime       = '2018-04-01 00:00:00+01' -- BST 
WHERE name = 'BRES_2016';
