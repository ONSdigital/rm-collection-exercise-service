Set schema 'collectionexercise';

-- Sequence: supplementarydatasetidseq
-- DROP SEQUENCE supplementarydatasetidseq;
CREATE SEQUENCE supplementarydatasetidseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;

-- Table: supplementarydataset
CREATE TABLE supplementarydataset
( id         int NOT NULL,
  exercise_FK int NOT NULL,
  supplementary_dataset_id  uuid NOT NULL,
  attributes character varying(100) NOT NULL
);

-- Add foreign key
ALTER TABLE ONLY supplementarydataset ADD CONSTRAINT exercise_FK_fkey FOREIGN KEY (exercise_FK) REFERENCES collectionexercise(exercise_PK);

-- Add index
ALTER TABLE supplementarydataset ADD CONSTRAINT supplementarydataset_id_uuid_key UNIQUE (id);