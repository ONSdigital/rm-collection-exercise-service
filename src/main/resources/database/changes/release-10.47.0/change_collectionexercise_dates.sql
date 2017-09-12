SET schema 'collectionexercise';

UPDATE collectionexercise.collectionexercise
SET
  scheduledreturndatetime    = '2017-10-06 01:00:00+01'
 ,scheduledenddatetime       = '2018-06-30 00:00:00+01' -- BST
WHERE name = 'BRES_2017';
