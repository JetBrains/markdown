#!/bin/sh
dirname=`dirname $0`
cd $dirname
java -Dfile.encoding=UTF-8 -jar build/libs/markdown-test.jar
