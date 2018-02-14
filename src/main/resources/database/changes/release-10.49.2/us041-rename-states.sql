INSERT INTO collectionexercise.collectionexercisestate (statepk) VALUES ('CREATED');
INSERT INTO collectionexercise.collectionexercisestate (statepk) VALUES ('EXECUTION_STARTED');

UPDATE collectionexercise.collectionexercise SET statefk = 'CREATED' WHERE statefk = 'INIT';
UPDATE collectionexercise.collectionexercise SET statefk = 'EXECUTION_STARTED' WHERE statefk = 'PENDING';

DELETE FROM collectionexercise.collectionexercisestate WHERE statepk = 'INIT';
DELETE FROM collectionexercise.collectionexercisestate WHERE statepk = 'PENDING';
