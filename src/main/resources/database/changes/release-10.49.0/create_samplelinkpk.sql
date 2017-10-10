SET schema 'collectionexercise';

CREATE SEQUENCE samplelinkpkseq
   START WITH 1
   INCREMENT BY 1
   NO MINVALUE
   MAXVALUE 999999999999
   CACHE 1;

ALTER TABLE collectionexercise.samplelink ADD COLUMN samplelinkPK bigint NOT NULL;
ALTER TABLE collectionexercise.samplelink ADD CONSTRAINT samplelinkPK_pkey PRIMARY KEY (samplelinkPK);
