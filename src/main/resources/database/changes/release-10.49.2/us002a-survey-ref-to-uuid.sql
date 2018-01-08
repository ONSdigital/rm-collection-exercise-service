
ALTER TABLE collectionexercise.collectionexercise ADD COLUMN survey_uuid UUID;

UPDATE collectionexercise.collectionexercise AS ce
SET survey_uuid = subquery.id
FROM (
    SELECT surveypk, id
    FROM collectionexercise.survey
) AS subquery
WHERE ce.surveyfk = subquery.surveypk;

ALTER TABLE collectionexercise.collectionexercise DROP COLUMN surveyfk;