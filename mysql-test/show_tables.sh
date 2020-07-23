#!/bin/sh

#if [ $# -eq 0 ]; then
#   bin=$(basename $0)
#   echo 'Usage:'
#   echo "   $bin <db>"
#   exit 0
#fi

#source './mysql-test.alias'
. './mysql-test.alias'

database=$(mysql-test "select database()")

#if [ ! $database ];then
#  echo 'no database selected"
#  exit 0
#fi


sql="select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='$database' and TABLE_NAME <> 'schema_version'"

mysql-test "$sql" 

