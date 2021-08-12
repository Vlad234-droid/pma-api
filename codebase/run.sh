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

export SECURITY_ENABLED=false
export SECURITY_OVERRIDING_ROLES_ENABLED=true
export FILE_STORAGE_ROOT="$SERVICE_HOME/demo/files"
export TESCO_API_BASE_URL=https://api-ppe.tesco.com
export ONE_LOGIN_BASE_URL=https://loginppe.ourtesco.com
export AD_ROLE_ADMIN=GG-UK-TescoGlobal-PMA-PPE-Admin
export AD_ROLE_LINE_MANAGER=GG-UK-TescoGlobal-PMA-PPE-LineManager

$JAVA -Dloader.path=application/src/main/image/config -jar application/build/libs/application-1.0.0-SNAPSHOT-boot.jar $@
