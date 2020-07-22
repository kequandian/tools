#!/bin/sh
## clean up the data in table t_category_report & t_sale_report which is month ago

BASEDIR=`readlink -e $(dirname $0)`

$BASEDIR/xtab.sh t_sale_report_his t_sale_report_his "report_date<DATE_ADD(CURDATE(),INTERVAL-30 DAY)" DELETE
$BASEDIR/xtab.sh t_category_report_his t_category_report_his "report_date<DATE_ADD(CURDATE(),INTERVAL-30 DAY)" DELETE
