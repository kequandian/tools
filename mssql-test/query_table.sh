#!/bin/sh

root=$(dirname $0)

if [ $# -eq 0 ]; then
   echo 'Usage:'
   echo '  query_table.sh <table-name> [top]'
   exit 0
fi

table=$1
start=$2
end=$3

if [ $# -eq 1 ];then
   $root/run_sql.sh "select * from $table"
elif [ $# -eq 3 ];then
   rows=$[$end-$start]
   sql="select top $rows \* from ( select row_number() over(order by id) as rownumber, \* from $table) where rownumber > $end"
   "$root/run_sql.sh \"$sql\""
elif [ $# -eq 2 ];then
   $root/run_sql.sh "select top $start * from $table"
fi

