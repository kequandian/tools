#!/bin/bash

#java -cp ./build/libs/mysqlxtab.jar:./libs/mysql-connector-java-5.1.5-bin.jar MySqlXTab "jdbc:mysql://120.76.244.80:3308/?useUnicode=true&characterEncoding=utf-8&user=root&password=root" $@
 
dir=$(dirname $0)
. "$dir/xtab.rc"

stable=$1
dtable=$2

if [ ! $dtable ];then
  dtable=$stable
fi

if [ ! $dtable ];then
   echo "Usage: $(basename $0) <src-table> <dest-table>"
   exit 0
fi


java -cp $dir/build/libs/mysqlxtab.jar:$dir/libs/mysql-connector-java-5.1.5-bin.jar MySqlXTab $sconn $stable WHERE $swhere TO $dconn $dtable

