#!/bin/bash
/opt/java/jdk1.7.0_60/bin/java -cp ./build/libs/db-convert.jar:./jars/mysql-connector-java-5.1.5-bin.jar:./jars/sqlite-jdbc-3.7.2.jar com.ericsson.codepilot.DBConvert "jdbc:mysql://localhost/journal?user=root&password=root" "jdbc:sqlite:/codepilot/codepilot.db"

