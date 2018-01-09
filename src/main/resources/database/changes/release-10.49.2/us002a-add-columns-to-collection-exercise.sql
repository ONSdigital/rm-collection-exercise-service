
ALTER TABLE collectionexercise.collectionexercise ADD user_description VARCHAR(50);
ALTER TABLE collectionexercise.collectionexercise ADD created TIMESTAMP;
ALTER TABLE collectionexercise.collectionexercise ADD updated TIMESTAMP;
ALTER TABLE collectionexercise.collectionexercise ADD deleted BOOLEAN DEFAULT FALSE;
