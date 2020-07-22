#!/bin/sh

source mssql-test.alias

root=$(dirname $0)

tables=$(mssql-test "select TABLE_NAME from INFORMATION_SCHEMA.TABLES")

for tab in $tables
do 
   tab=${tab%\'}
   tab=${tab#\'}

   echo "--$tab"
   $root/query_table.sh $tab
done

