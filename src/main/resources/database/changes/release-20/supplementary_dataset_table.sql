Set schema 'collectionexercise';

-- Table: supplementarydataset
CREATE TABLE supplementarydataset
( id         int NOT NULL UNIQUE,
  exercise_FK int NOT NULL UNIQUE,
  supplementary_dataset_id  uuid NOT NULL UNIQUE,
  attributes character varying(10000) NOT NULL
);

-- -- Add foreign key
ALTER TABLE supplementarydataset ADD CONSTRAINT exercise_FK_fkey FOREIGN KEY (exercise_FK) REFERENCES collectionexercise(exercise_PK);
