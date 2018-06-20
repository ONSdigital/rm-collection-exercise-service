DO $$
DECLARE
  start_casetypedefault_pk integer;

BEGIN
  -- get current max pk count + 1 for each table
  SELECT COALESCE(MAX(casetypedefaultpk), 0)+1 INTO start_casetypedefault_pk FROM collectionexercise.casetypedefault;

  EXECUTE 'CREATE SEQUENCE IF NOT EXISTS collectionexercise.casetypedefaultseq
  			  START WITH ' || start_casetypedefault_pk ||
          'INCREMENT BY 1
          OWNED BY collectionexercise.casetypedefault.casetypedefaultpk;';

END $$;