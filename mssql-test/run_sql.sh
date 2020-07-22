#!/bin/sh
#shopt -s expand_aliases

if [ $# -eq 0 ]; then
   echo 'Usage:'
   echo '   run_sql.sh <sql>'
   exit 0
fi

#source './mssql-test.alias'
. './mssql-test.alias'

mssql-test "$@"
