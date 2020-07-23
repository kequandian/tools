#!/bin/sh

if [ $# -eq 0 ]; then
   echo 'Usage:'
   echo '   query_table_count.sh <table-name>'
   exit 0
fi

table_name=$1
./run_sql.sh "select count(*) from $table_name"
