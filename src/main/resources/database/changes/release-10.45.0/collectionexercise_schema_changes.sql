
CREATE TABLE collectionexercise.collectionexercisestate AS SELECT * FROM collectionexercise.state;
ALTER TABLE collectionexercise.collectionexercisestate ADD CONSTRAINT collectionexercise_statepk_pkey PRIMARY KEY(statepk);

CREATE TABLE collectionexercise.sampleunitgroupstate AS SELECT * FROM collectionexercise.state WHERE statepk <> 'PENDING';
ALTER TABLE collectionexercise.sampleunitgroupstate ADD CONSTRAINT sampleunitgroup_statepk_pkey PRIMARY KEY(statepk);

ALTER TABLE collectionexercise.collectionexercise  DROP CONSTRAINT statefk_fkey CASCADE;
ALTER TABLE collectionexercise.sampleunitgroup     DROP CONSTRAINT statefk_fkey CASCADE;

ALTER TABLE collectionexercise.collectionexercise ADD CONSTRAINT statefk_fkey FOREIGN KEY (statefk) REFERENCES collectionexercise.collectionexercisestate (statepk);
ALTER TABLE collectionexercise.sampleunitgroup ADD CONSTRAINT statefk_fkey FOREIGN KEY (statefk) REFERENCES collectionexercise.sampleunitgroupstate (statepk);


DROP TABLE collectionexercise.state;