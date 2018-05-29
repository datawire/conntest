FROM openjdk:10-jdk-slim as build
MAINTAINER Datawire Engineering <dev@datawire.io>

VOLUME /root/.gradle
WORKDIR /src
COPY . .
RUN ./gradlew test fatJar

FROM openjdk:10-jdk-slim as runtime
MAINTAINER Datawire Engineering <dev@datawire.io>
LABEL PROJECT_REPO_URL         = "git@github.com:datawire/conntest.git" \
      PROJECT_REPO_BROWSER_URL = "https://github.com/datawire/conntest" \
      DESCRIPTION              = "Datawire Connection Test Service" \
      VENDOR                   = "Datawire, Inc." \
      VENDOR_URL               = "https://datawire.io" \
      PROJECT_URL              = "https://github.com/datawire/conntest"

WORKDIR /srv
COPY --from=build /src/build/libs/conntest-fat.jar .

ENTRYPOINT ["java"]
CMD ["-jar", "conntest-fat.jar"]