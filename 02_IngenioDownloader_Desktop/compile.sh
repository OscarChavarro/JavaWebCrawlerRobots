#!/bin/bash
export LC_ALL=en_US.UTF-8
cd src
javac -Xlint:deprecation -Xlint:unchecked -d ../classes -classpath ../lib/commons-logging-1.2.jar:../lib/httpclient-4.4.1.jar:../lib/httpcore-4.4.1.jar:../lib/httpmime-4.4.1.jar:../lib/mongo-java-driver-2.13.2.jar:../lib/mongo-java-driver-2.13.2.jar `find -L . -type f`
