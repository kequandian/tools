#!/bin/sh

BASEDIR=`readlink -e $(dirname $0)`

$BASEDIR/xtab.sh t_category_report t_category_report_his "report_date<DATE_ADD(CURDATE(),INTERVAL-3 DAY)"
$BASEDIR/xtab.sh t_category_report t_category_report "report_date<DATE_ADD(CURDATE(),INTERVAL-3 DAY)" DELETE
