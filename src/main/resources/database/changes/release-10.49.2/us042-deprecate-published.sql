UPDATE collectionexercise.collectionexercise AS c
 SET statefk = 'LIVE'
 FROM collectionexercise.event AS e
 WHERE c.exercisepk = e.collexfk
 AND (tag = 'go_live' and timestamp <= now())
 OR c.actualpublishdatetime <= now()
 AND statefk = 'PUBLISHED';

UPDATE collectionexercise.collectionexercise AS c
 SET statefk = 'READY_FOR_LIVE'
 FROM collectionexercise.event AS e
 WHERE c.exercisepk = e.collexfk
 AND (tag = 'go_live' and timestamp > now())
 OR c.actualpublishdatetime > now()
 AND statefk = 'PUBLISHED';

DELETE FROM collectionexercise.collectionexercisestate WHERE statepk = 'PUBLISHED';