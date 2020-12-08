UPDATE collectionexercise.collectionexercise AS c
SET statefk = 'SCHEDULED'
FROM collectionexercise.event AS e
WHERE c.exercisepk = e.collexfk
  AND c.statefk = 'NOT_SET'
  AND e.timestamp >= now()
  AND e.timestamp IS NOT NULL;

UPDATE collectionexercise.collectionexercise AS c
SET statefk = 'PROCESSED'
FROM collectionexercise.event AS e
WHERE c.exercisepk = e.collexfk
  AND c.statefk = 'NOT_SET'
  AND e.timestamp < now()
  AND e.timestamp IS NOT NULL;
