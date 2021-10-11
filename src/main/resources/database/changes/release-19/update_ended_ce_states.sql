UPDATE collectionexercise.collectionexercise ce
SET state_fk='ENDED'
from collectionexercise."event" ev
WHERE ce.state_fk = 'LIVE' and ce.exercise_pk = ev.exercise_fk and ev.tag = 'exercise_end' and ev."timestamp" <= now() and ev."status" = 'PROCESSED';