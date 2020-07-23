#!/bin/bash

dir=$(dirname $0)
$dir/run_sql.sh "select now()"
