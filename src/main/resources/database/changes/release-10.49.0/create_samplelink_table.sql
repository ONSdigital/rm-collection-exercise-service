-- Collection Excercise schema change CTPA-1649
-- Add a table to allow linking of multiple sample files to a collection exercise
-- and allow collection exercises to share sample files

-- Table: samplelink
CREATE TABLE collectionexercise.samplelink
( collectionexerciseID  uuid,
  samplesummaryID       uuid
);