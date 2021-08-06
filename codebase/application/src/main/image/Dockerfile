FROM        ${BASE_IMAGE_URL}:${BASE_IMAGE_VERSION}
MAINTAINER  ${MAINTAINER}
ENV         JAVA_OPTS=$JAVA_OPTS
ENV         JAVA_ARGS=$JAVA_ARGS
ENV         SERVICE_USER=service
ENV         SERVICE_HOME=/opt/${SERVICE_USER}
RUN         adduser -h  ${SERVICE_HOME} -g "Spring boot service user" -s /bin/sh -D ${SERVICE_USER} && \
            mkdir -p ${SERVICE_HOME}/logs \
            mkdir -p ${SERVICE_HOME}/config
COPY        ${SERVICE_JAR} ${SERVICE_HOME}
COPY        config ${SERVICE_HOME}/config
RUN         chown ${SERVICE_USER}:${SERVICE_USER} ${SERVICE_HOME}/*
USER        1000
WORKDIR     ${SERVICE_HOME}
CMD         ${JAVA_HOME}/bin/java -Dloader.path=${SERVICE_HOME}/config ${JAVA_OPTS} -jar ${SERVICE_JAR} ${JAVA_ARGS}
