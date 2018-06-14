DO $$
DECLARE
  start_casetypeoverride_pk integer;

BEGIN
  -- get current max pk count + 1 for each table
  SELECT COALESCE(MAX(casetypeoverridepk), 0)+1 INTO start_casetypeoverride_pk FROM collectionexercise.casetypeoverride;

  EXECUTE 'CREATE SEQUENCE IF NOT EXISTS collectionexercise.casetypeoverrideseq
  			  START WITH ' || start_casetypeoverride_pk ||
          'INCREMENT BY 1
          OWNED BY collectionexercise.casetypeoverride.casetypeoverridepk;';

END $$;