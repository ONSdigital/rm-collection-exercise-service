Set schema 'collectionexercise';

-- Table: supplementarydataset
CREATE TABLE supplementarydataset
( id         int NOT NULL,
  exercise_FK int NOT NULL,
  supplementary_dataset_id  uuid NOT NULL,
  attributes character varying(100) NOT NULL
);

-- -- Add foreign key
-- ALTER TABLE ONLY supplementarydataset ADD CONSTRAINT exercise_FK_fkey FOREIGN KEY (exercise_FK) REFERENCES collectionexercise(exercise_PK);

-- Add index
ALTER TABLE supplementarydataset ADD CONSTRAINT supplementarydataset_id_uuid_key UNIQUE (id);

-- unique supplementary_dataset_id
ALTER TABLE supplementarydataset ADD CONSTRAINT supplementary_dataset_id_unique UNIQUE (supplementary_dataset_id);

-- unique exercise_FK
ALTER TABLE supplementarydataset ADD CONSTRAINT exercise_FK_unique UNIQUE (exercise_FK);