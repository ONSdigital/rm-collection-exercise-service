UPDATE collectionexercise.event AS e
SET status = 'SCHEDULED'
WHERE e.status = 'NOT_SET'
  AND e.timestamp >= now()
  AND e.timestamp IS NOT NULL;

UPDATE collectionexercise.event AS e
SET status = 'PROCESSED'
WHERE e.status = 'NOT_SET'
  AND e.timestamp < now()
  AND e.timestamp IS NOT NULL;
