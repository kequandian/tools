#!/bin/sh

#java -cp ./build/libs/bric3-jd-intellij.jar:./lib/jd-common-0.7.1.jar:./lib/jd-common-ide-0.7.1.jar:./lib/jd-core-0.7.1.jar jd.ide.intellij.JavaDecompilerTest hello.jar "de/HelloWorld.class"
 
java -cp ./build/libs/bric3-jd-intellij.jar:./lib/jd-common-0.7.1.jar:./lib/jd-common-ide-0.7.1.jar:./lib/jd-core-0.7.1.jar jd.ide.intellij.JavaDecompilerTest $1 $2
