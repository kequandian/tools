#!/bin/sh

if [ $# -eq 0 ]; then
   echo 'Usage:'
   echo '  query_table_count.sh <table-name> [start] [end]'
   exit 0
fi

table_name=$1
start=$2
end=$3

./run_sql.sh "select * from $table_name limit $start $end"
