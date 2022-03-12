#!/bin/sh

[ -d /etc/app/properties ] && cp /etc/app/properties/* /app/config

java \
    -Dloader.path=$SERVICE_HOME/config \
     $JAVA_OPTS \
     -jar ./app.jar $JAVA_ARGS
