UPDATE collectionexercise.collectionexercise AS c
 SET statefk = 'LIVE'
 FROM collectionexercise.event AS e
 WHERE c.exercisepk = e.collexfk
 AND statefk = 'PUBLISHED'
 AND (tag = 'go_live' and timestamp <= now())
 OR (c.actualpublishdatetime <= now()
 AND c.actualpublishdatetime IS NOT NULL);

 UPDATE collectionexercise.collectionexercise AS c
 SET statefk = 'READY_FOR_LIVE'
 FROM collectionexercise.event AS e
 WHERE c.exercisepk = e.collexfk
 AND statefk = 'PUBLISHED'
 AND (tag = 'go_live' and timestamp > now())
 OR (c.actualpublishdatetime > now()
 AND c.actualpublishdatetime IS NOT NULL);

DELETE FROM collectionexercise.collectionexercisestate WHERE statepk = 'PUBLISHED';