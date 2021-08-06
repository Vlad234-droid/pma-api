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

$JAVA -Dloader.path=application/src/main/image/config -jar application/build/libs/application-1.0.0-SNAPSHOT.jar $@
