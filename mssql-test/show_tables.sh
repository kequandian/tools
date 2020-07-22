#!/bin/sh

source mssql-test.alias

tables=$(mssql-test "select TABLE_NAME from INFORMATION_SCHEMA.TABLES")

for tab in $tables
do 
   tab=${tab%\'}
   tab=${tab#\'}
   echo $tab
done

