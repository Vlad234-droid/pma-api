FROM openjdk:11-jdk-slim as build

ARG BUILD_PROFILES=default,docker

ENV GRADLE_HOME=/opt/gradle
ENV GRADLE_VERSION=6.8.3
ENV GRADLE_DOWNLOAD_SHA256=7faa7198769f872826c8ef4f1450f839ec27f0b4d5d1e51bade63667cbccd205

RUN set -o errexit -o nounset \
    \
    && echo "Adding gradle user and group" \
    && groupadd --system --gid 1000 gradle \
    && useradd --system --gid gradle --uid 1000 --shell /bin/bash --create-home gradle \
    && mkdir /home/gradle/.gradle \
    && chown --recursive gradle:gradle /home/gradle \
    \
    && echo "Sym-linking root Gradle cache to gradle Gradle cache" \
    && ln -s /home/gradle/.gradle /root/.gradle

VOLUME /home/gradle/.gradle

WORKDIR /home/gradle

RUN apt-get update \
    && apt-get install --yes --no-install-recommends \
        dos2unix \
        fontconfig \
        unzip \
        wget \
        bzr \
        git \
        git-lfs \
        mercurial \
        openssh-client \
        subversion \
    && rm -rf /var/lib/apt/lists/*

RUN set -o errexit -o nounset \
    && echo "Downloading Gradle" \
    && wget --no-verbose --output-document=gradle.zip \
       "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
    \
    && echo "Checking download hash" \
    && echo "${GRADLE_DOWNLOAD_SHA256} *gradle.zip" | sha256sum --check - \
    \
    && echo "Installing Gradle" \
    && unzip gradle.zip \
    && rm gradle.zip \
    && mv "gradle-${GRADLE_VERSION}" "${GRADLE_HOME}/" \
    && ln --symbolic "${GRADLE_HOME}/bin/gradle" /usr/bin/gradle \
    \
    && echo "Testing Gradle installation" \
    && gradle --version \
    && mkdir -p /home/gradle/app

COPY --chown=gradle:gradle --chmod=0755 ./scripts/start.sh /home/gradle/app
COPY --chown=gradle:gradle ./codebase/ /home/gradle/app

WORKDIR /home/gradle/app

# Build app, and skip tests
RUN gradle build --no-daemon -PbuildProfiles=$BUILD_PROFILES -x test \
    && dos2unix /home/gradle/app/start.sh

# ==========
# main stage
# ==========
FROM openjdk:11-jdk-slim

ARG RUNTIME_JAVA_OPTS ""
ARG RUNTIME_JAVA_ARGS ""

ENV JAVA_OPTS $RUNTIME_JAVA_OPTS
ENV JAVA_ARGS $RUNTIME_JAVA_ARGS

ENV SERVICE_HOME /app
RUN mkdir $SERVICE_HOME

COPY --from=build /home/gradle/app/start.sh $SERVICE_HOME
COPY --from=build /home/gradle/app/application/build/libs/*.jar $SERVICE_HOME/app.jar
COPY --from=build /home/gradle/app/application/build/libs/config $SERVICE_HOME/config

EXPOSE 8083/tcp
EXPOSE 8090/tcp

WORKDIR $SERVICE_HOME

CMD java -Dloader.path=$SERVICE_HOME/config,$SERVICE_HOME/properties $JAVA_OPTS -jar ./app.jar $JAVA_ARGS
