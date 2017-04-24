-- Need conditional test for Revoke if rolename doesn't already exist 

DO $$
BEGIN
IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='collectionexercisesvc') THEN
   REVOKE ALL PRIVILEGES ON DATABASE postgres FROM collectionexercisesvc;
END IF;
END$$;

DROP SCHEMA IF EXISTS collectionexercise CASCADE;
DROP ROLE IF EXISTS collectionexercisesvc;

CREATE USER collectionexercisesvc PASSWORD 'collectionexercisesvc'
  NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION INHERIT LOGIN;

CREATE SCHEMA collectionexercise AUTHORIZATION collectionexercisesvc;

REVOKE ALL ON ALL TABLES IN SCHEMA collectionexercise FROM PUBLIC;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA collectionexercise FROM PUBLIC;
REVOKE CONNECT ON DATABASE postgres FROM PUBLIC;

GRANT CONNECT ON DATABASE postgres TO collectionexercisesvc;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA collectionexercise TO collectionexercisesvc;
GRANT ALL ON ALL SEQUENCES IN SCHEMA collectionexercise TO collectionexercisesvc;