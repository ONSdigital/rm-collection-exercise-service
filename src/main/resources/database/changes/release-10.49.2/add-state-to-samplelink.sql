-- Table: state
CREATE TABLE collectionexercise.samplelinkstate
(
  statePK character varying(20) NOT NULL PRIMARY KEY
);

INSERT INTO collectionexercise.samplelinkstate ( statePK ) VALUES ('INIT');
INSERT INTO collectionexercise.samplelinkstate ( statePK ) VALUES ('ACTIVE');

ALTER TABLE collectionexercise.samplelink ADD COLUMN statefk CHARACTER VARYING(20);

-- Assume any existing samplelinks are ACTIVE
UPDATE collectionexercise.samplelink SET statefk = 'ACTIVE';

ALTER TABLE collectionexercise.samplelink
ADD CONSTRAINT sl_statefk_fkey FOREIGN KEY (statefk) REFERENCES collectionexercise.samplelinkstate(statePK);

ALTER TABLE collectionexercise.samplelink ALTER COLUMN statefk SET NOT NULL;



