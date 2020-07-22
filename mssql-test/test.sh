#!/bin/sh

if [ ! -f build/libs/mssql-test.jar ]; then
   gradle build
fi

./run_sql.sh "select getdate()"
