DO $$
DECLARE
  statements CURSOR FOR
    SELECT    schemaname, tablename
    FROM      pg_tables
    WHERE     schemaname in (
                'admin',
                'analytics',
                'billing',
                'contract',
                'file',
                'logging',
                'messaging',
                'order',
                'provider',
                'rating',
                'web'
              ) and
              tablename not in ( -- Ignore tables with records inserted by repeatable migrations
                'asset_file_type',
                'asset_metadata_property',
                'mail_template',
                'notification_template',
                'settings'
              )
    ORDER BY  schemaname, tablename;
BEGIN
  FOR s IN statements LOOP
      -- RAISE NOTICE 'Statement: %', 'TRUNCATE TABLE ' || quote_ident(s.schemaname) || '.' || quote_ident(s.tablename) || ' CASCADE;';

      EXECUTE 'TRUNCATE TABLE ' || quote_ident(s.schemaname) || '.' || quote_ident(s.tablename) || ' CASCADE;';
  END LOOP;
END $$;

