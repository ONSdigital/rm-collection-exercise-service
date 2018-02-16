UPDATE collectionexercise.collectionexercise AS c
 SET statefk = 'LIVE'
 FROM collectionexercise.event AS e
 WHERE c.exercisepk = e.collexfk
 AND c.statefk = 'PUBLISHED'
 AND e.tag = 'go_live'
 AND e.timestamp <= now();

UPDATE collectionexercise.collectionexercise AS c
 SET statefk = 'READY_FOR_LIVE'
 FROM collectionexercise.event AS e
 WHERE c.exercisepk = e.collexfk
 AND c.statefk = 'PUBLISHED'
 AND e.tag = 'go_live'
 AND e.timestamp > now();

-- Catch those which don't have events
UPDATE collectionexercise.collectionexercise AS c
 SET statefk = 'LIVE'
 WHERE statefk = 'PUBLISHED'
 AND c.actualpublishdatetime <= now()
 AND c.actualpublishdatetime IS NOT NULL;

-- Catch those which don't have events
UPDATE collectionexercise.collectionexercise AS c
 SET statefk = 'READY_FOR_LIVE'
 WHERE c.statefk = 'PUBLISHED'
 AND c.actualpublishdatetime > now()
 AND c.actualpublishdatetime IS NOT NULL;


DELETE FROM collectionexercise.collectionexercisestate WHERE statepk = 'PUBLISHED';