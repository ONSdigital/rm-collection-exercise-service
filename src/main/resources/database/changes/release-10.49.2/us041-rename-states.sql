INSERT INTO collectionexercise.collectionexercisestate (statepk) VALUES ('CREATED');
INSERT INTO collectionexercise.collectionexercisestate (statepk) VALUES ('EXECUTION_STARTED');

UPDATE collectionexercise.collectionexercise SET statefk = 'CREATED' WHERE statefk = 'INIT';
UPDATE collectionexercise.collectionexercise SET statefk = 'EXECUTION_STARTED' WHERE statefk = 'PENDING';
UPDATE collectionexercise.collectionexercise
    SET statefk = 'SCHEDULED'
    WHERE exercisepk IN
        (SELECT event.collexfk
         FROM collectionexercise.event
         WHERE event.tag IN ('go_live',
                       'mps',
                       'return_by',
                       'exercise_end')
         GROUP BY event.collexfk
         HAVING count(*) = 4);

DELETE FROM collectionexercise.collectionexercisestate WHERE statepk = 'INIT';
DELETE FROM collectionexercise.collectionexercisestate WHERE statepk = 'PENDING';
