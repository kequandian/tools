#!/bin/sh
#shopt -s expand_aliases


if [ $# -eq 0 ]; then
   echo 'Usage:'
   echo '   desc_table.sh <table-name>'
   exit 0
fi

./run_sql_with.pl desc.table.sql $1


#__SQL__
#SELECT c.TABLE_NAME, 
#          c.COLUMN_NAME, 
#          c.DATA_TYPE, 
#          c.Column_default, 
#          c.character_maximum_length, 
#          c.numeric_precision, 
#          c.is_nullable,
#          CASE 
#            WHEN u.CONSTRAINT_TYPE = 'PRIMARY KEY' THEN 'primary key'
#            ELSE '' 
#          END AS KeyType
#     FROM INFORMATION_SCHEMA.COLUMNS as c
#LEFT JOIN information_schema.table_constraints as u ON c.table_name = u.table_name
#ORDER BY table_name
#
