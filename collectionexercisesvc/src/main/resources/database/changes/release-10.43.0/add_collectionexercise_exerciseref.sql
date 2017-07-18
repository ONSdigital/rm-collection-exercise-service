
SET schema 'collectionexercise';

ALTER TABLE collectionexercise.collectionexercise ADD COLUMN exerciseref character varying(20);

UPDATE collectionexercise.collectionexercise
SET exerciseref = '221_201712';


ALTER TABLE collectionexercise.collectionexercise ALTER COLUMN exerciseref SET NOT NULL;