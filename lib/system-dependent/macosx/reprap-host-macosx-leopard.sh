#!/bin/sh

export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home
export PATH=$JAVA_HOME/bin:$PATH
java -jar reprap.jar
