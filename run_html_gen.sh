#!/bin/sh
dirname=`dirname $0`
cd $dirname
java -Dfile.encoding=UTF-8 -classpath "out/test/intellij-markdown:out/production/intellij-markdown:lib/kotlin-runtime.jar:lib/junit.jar:lib/idea_rt.jar"  org.intellij.markdown.SpecRunner
