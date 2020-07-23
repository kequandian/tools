SELECT c.TABLE_NAME, c.COLUMN_NAME, c.DATA_TYPE, c.CHARACTER_MAXIMUM_LENGTH, c.NUMERIC_PRECISION, c.IS_NULLABLE, c.COLUMN_DEFAULT, 
      CASE WHEN pk.COLUMN_NAME IS NOT NULL THEN pk.CONSTRAINT_TYPE ELSE '' END AS KeyType 
	  FROM INFORMATION_SCHEMA.COLUMNS c LEFT JOIN 
	  (SELECT tc.CONSTRAINT_TYPE,
			  ku.TABLE_CATALOG,
			  ku.TABLE_SCHEMA,
			  ku.TABLE_NAME,
			  ku.COLUMN_NAME  
			  FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
			  AS tc  
		INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS ku ON tc.CONSTRAINT_TYPE IS NOT NULL AND tc.CONSTRAINT_NAME = ku.CONSTRAINT_NAME) as pk 
		ON c.TABLE_CATALOG = pk.TABLE_CATALOG  AND c.TABLE_SCHEMA = pk.TABLE_SCHEMA  AND c.TABLE_NAME = pk.TABLE_NAME  AND c.COLUMN_NAME = pk.COLUMN_NAME 
		WHERE c.TABLE_NAME='$1'
		ORDER BY c.TABLE_SCHEMA,c.TABLE_NAME, c.ORDINAL_POSITION
