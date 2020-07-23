#!/bin/sh

#source './mysql-test.alias'
. './mysql-test.alias'

sql="SELECT schema_name FROM information_schema.schemata;"

mysql-test "$sql" 

