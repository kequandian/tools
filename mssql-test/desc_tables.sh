#!/bin/sh

source mssql-test.alias

root=$(dirname $0)

tables=$(mssql-test "select TABLE_NAME from INFORMATION_SCHEMA.TABLES")

for tab in $tables
do 
   tab=${tab%\'}
   tab=${tab#\'}

   $root/run_sql_with.pl desc.table.sql $tab
done

