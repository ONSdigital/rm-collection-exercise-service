UPDATE collectionexercise.collectionexercise SET statefk = 'READY_FOR_LIVE' WHERE statefk = 'PUBLISHED';

DELETE FROM collectionexercise.collectionexercisestate WHERE statepk = 'PUBLISHED';