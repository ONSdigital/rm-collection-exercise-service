
ALTER TABLE collectionexercise.casetypedefault ADD COLUMN survey_uuid UUID;

UPDATE collectionexercise.casetypedefault AS ce
SET survey_uuid = subquery.id
FROM (
    SELECT surveypk, id
    FROM collectionexercise.survey
) AS subquery
WHERE ce.surveyfk = subquery.surveypk;

ALTER TABLE collectionexercise.casetypedefault DROP COLUMN surveyfk;