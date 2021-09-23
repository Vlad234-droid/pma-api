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
export REST_TEMPLATE_SECURITY_ENABLED=true
export SECURITY_OVERRIDING_ROLES_ENABLED=true
export FILE_STORAGE_ROOT="$SERVICE_HOME/demo/files"
export TESCO_API_BASE_URL=https://api-ppe.tesco.com
export ONE_LOGIN_BASE_URL=https://loginppe.ourtesco.com
export AD_ROLE_ADMIN=GG-UK-TescoGlobal-PMA-PPE-Admin
export AD_ROLE_LINE_MANAGER=GG-UK-TescoGlobal-PMA-PPE-LineManager

export IDENTITY_CLIENT_ID=78fe5149-d011-423d-b218-dd3cc967508c
export IDENTITY_CLIENT_SECRET=7b7d31fe-544a-4467-b6fa-76ae885604de

export TESCO_CAPI_URL=http://localhost:9080/colleague
export TESCO_CFAPI_URL=http://localhost:9080/colleague/v2/colleagues

$JAVA -Dloader.path=demo/config -jar application/build/libs/application-1.0.0-SNAPSHOT-boot.jar $@
# $JAVA -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Dloader.path=demo/config -jar application/build/libs/application-1.0.0-SNAPSHOT-boot.jar $@
