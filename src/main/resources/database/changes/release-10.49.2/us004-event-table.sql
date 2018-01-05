CREATE TABLE collectionexercise.event (
  id uuid UNIQUE,
  eventpk bigint PRIMARY KEY,
  collexfk bigint REFERENCES collectionexercise.collectionexercise(exercisepk),
  tag VARCHAR(20),
  timestamp TIMESTAMP,
  created TIMESTAMP,
  update TIMESTAMP,
  deleted BOOLEAN,
  UNIQUE(collexfk, tag)
);


