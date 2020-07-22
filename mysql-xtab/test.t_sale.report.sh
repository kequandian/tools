#!/bin/sh

BASEDIR=`readlink -e $(dirname $0)`

$BASEDIR/xtab.sh t_sale_report t_sale_report_his "report_date<DATE_ADD(CURDATE(),INTERVAL-3 DAY)"
$BASEDIR/xtab.sh t_sale_report t_sale_report "report_date<DATE_ADD(CURDATE(),INTERVAL-3 DAY)" DELETE
