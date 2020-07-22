#!/bin/sh

root=$(dirname $0)

if [ $# -eq 0 ]; then
   echo 'Usage:'
   echo '   query_table_count.sh <table-name>'
   exit 0
fi

$root/run_sql.sh "select count(*) from $1"
