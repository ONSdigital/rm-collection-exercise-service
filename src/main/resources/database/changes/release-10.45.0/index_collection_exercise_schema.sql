
-- COLLECTION EXERCISE SERVICE

---------------------------------------------------------------------------
---------------------------------------------------------------------------

-- casetypedefault table


-- Index: collectionexercise.ctd_surveyfk_index
-- DROP INDEX collectionexercise.ctd_surveyfk_index;

CREATE INDEX ctd_surveyfk_index ON collectionexercise.casetypedefault USING btree (surveyfk);


-- Index: collectionexercise.ctd_sampleunittypefk_index
-- DROP INDEX collectionexercise.ctd_sampleunittypefk_index;

CREATE INDEX ctd_sampleunittypefk_index ON collectionexercise.casetypedefault USING btree (sampleunittypefk);


---------------------------------------------------------------------------
---------------------------------------------------------------------------

-- casetypeoverride table


-- Index: collectionexercise.exercisefk_index
-- DROP INDEX collectionexercise.exercisefk_index;

CREATE INDEX cto_exercisefk_index ON collectionexercise.casetypeoverride USING btree (exercisefk);


-- Index: collectionexercise.cto_sampleunittypefk_index
-- DROP INDEX collectionexercise.cto_sampleunittypefk_index;

CREATE INDEX cto_sampleunittypefk_index ON collectionexercise.casetypeoverride USING btree (sampleunittypefk);


---------------------------------------------------------------------------
---------------------------------------------------------------------------

-- collectionexercise table


-- Index: collectionexercise.collectionexercise_statefk_index
-- DROP INDEX collectionexercise.collectionexercise_statefk_index;

CREATE INDEX collectionexercise_statefk_index ON collectionexercise.collectionexercise USING btree (statefk);


-- Index: collectionexercise.collectionexercise_surveyfk_index
-- DROP INDEX collectionexercise.collectionexercise_surveyfk_index;

CREATE INDEX collectionexercise_surveyfk_index ON collectionexercise.collectionexercise USING btree (surveyfk);



---------------------------------------------------------------------------
---------------------------------------------------------------------------

-- sampleunit table


-- Index: collectionexercise.sampleunit_sampleunittypefk_index
-- DROP INDEX collectionexercise.sampleunit_sampleunittypefk_index;

CREATE INDEX sampleunit_sampleunittypefk_index ON collectionexercise.sampleunit USING btree (sampleunittypefk);


-- Index: collectionexercise.sampleunit_sampleunitgroupfk_index
-- DROP INDEX collectionexercise.sampleunit_sampleunitgroupfk_index;

CREATE INDEX sampleunit_sampleunitgroupfk_index ON collectionexercise.sampleunit USING btree (sampleunitgroupfk);

---------------------------------------------------------------------------
---------------------------------------------------------------------------

-- sampleunitgroup table


-- Index: collectionexercise.sampleunitgroup_statefk_index
-- DROP INDEX collectionexercise.sampleunitgroup_statefk_index;

CREATE INDEX sampleunitgroup_statefk_index ON collectionexercise.sampleunitgroup USING btree (statefk);


-- Index: collectionexercise.sampleunitgroup_exercisefk_index
-- DROP INDEX collectionexercise.sampleunitgroup_exercisefk_index;

CREATE INDEX sampleunitgroup_exercisefk_index ON collectionexercise.sampleunitgroup USING btree (exercisefk);

