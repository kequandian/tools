select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='$1' and TABLE_NAME <> 'schema_version'
