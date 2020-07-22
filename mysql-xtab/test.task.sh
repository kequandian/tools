#!/bin/sh

BASEDIR=`readlink -e $(dirname $0)`

$BASEDIR/x_t_sale_report.sh
$BASEDIR/x_t_category_report.sh
