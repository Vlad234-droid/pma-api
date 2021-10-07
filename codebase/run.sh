#!/bin/bash

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
      JAVA="$JAVA_HOME/bin/java"
    else
      JAVA="java"
      echo JAVA_HOME is not set. Unexpected results may occur.
      echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
    fi
fi

export SERVICE_HOME=$(pwd)
export java.io.tmpdir=$(pwd)/temp

$JAVA -Dloader.path=application/build/libs/config -jar application/build/libs/application-1.0.0-SNAPSHOT-boot.jar $@
# $JAVA -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Dloader.path=demo/config -jar application/build/libs/application-1.0.0-SNAPSHOT-boot.jar $@
