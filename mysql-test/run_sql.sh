#!/bin/sh
#shopt -s expand_aliases

if [ $# -eq 0 ]; then
   echo 'Usage:'
   echo '   run_sql.sh <sql>'
   exit 0
fi

dir=$(dirname $0)

#source "$dir/mysql-test.alias"
. "$dir/mysql-test.alias"

mysql-test "$@"
