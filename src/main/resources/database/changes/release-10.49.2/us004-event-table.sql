CREATE SEQUENCE collectionexercise.eventpkseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;

CREATE TABLE collectionexercise.event (
  id uuid UNIQUE,
  eventpk bigint PRIMARY KEY,
  collexfk bigint REFERENCES collectionexercise.collectionexercise(exercisepk),
  tag VARCHAR(20),
  timestamp TIMESTAMP,
  created TIMESTAMP,
  updated TIMESTAMP,
  deleted BOOLEAN,
  UNIQUE(collexfk, tag)
);


