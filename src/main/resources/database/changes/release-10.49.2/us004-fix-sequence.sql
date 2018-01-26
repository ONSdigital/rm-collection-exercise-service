-- Set the value of the sequence to greater number of the current sequence number or 4
SELECT SETVAL('collectionexercise.exercisepkseq', (SELECT
              GREATEST((SELECT last_value
                 FROM   collectionexercise.exercisepkseq), 4)));