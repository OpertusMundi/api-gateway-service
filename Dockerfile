# vim: set syntax=dockerfile:

FROM node:10.16.3-buster AS npm-build

ENV NPM_CONFIG_PROGRESS="false" \
    NPM_CONFIG_SPIN="false"

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN apt-get update && apt-get install -y --no-install-recommends jq moreutils \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY api-gateway/src/main/frontend /app/
RUN jq -f ./filter-deps-for-prod.jq package.json | sponge package.json
RUN npm install && npm run build


#FROM maven:3.6.3-openjdk-8 as maven-build
# see https://github.com/OpertusMundi/java-commons/blob/master/Dockerfile
FROM opertusmundi/java-commons-builder:1.0.27 as maven-build

WORKDIR /app

COPY common /app/common/
RUN (cd /app/common && mvn -B install)

COPY pom.xml /app/
COPY api-gateway/pom.xml /app/api-gateway/
RUN mvn -B dependency:resolve-plugins dependency:resolve
RUN mvn -B -pl api-gateway dependency:copy-dependencies -DincludeScope=runtime

COPY api-gateway/src/main/resources /app/api-gateway/src/main/resources
COPY api-gateway/src/main/java /app/api-gateway/src/main/java
COPY api-gateway/resources /app/api-gateway/resources
COPY --from=npm-build /app/dist /app/api-gateway/src/main/frontend/dist/
RUN mvn -B compile -DenableJavaBuildProfile -DenableDockerBuildProfile


FROM openjdk:8-jre-alpine

COPY --from=maven-build /app/api-gateway/target/ /app/

RUN addgroup spring && adduser -H -D -G spring spring

COPY docker-entrypoint.sh /usr/local/bin
RUN chmod a+x /usr/local/bin/docker-entrypoint.sh

WORKDIR /app

RUN mkdir config logs \
    && chgrp spring config logs \
    && chmod g=rwx config logs

ENV PUBLIC_URL="" \
    DATABASE_URL="jdbc:postgresql://db:5432/opertusmundi" \
    DATABASE_USERNAME="spring" \
    DATABASE_PASSWORD_FILE="/secrets/database-password" \
    JWT_SECRET_FILE="/secrets/jwt-signing-key" \
    BPM_REST_BASE_URL="http://bpm-server:8000/engine-rest" \
    BPM_REST_USERNAME="" \
    BPM_REST_PASSWORD_FILE="/secrets/bpm-rest-password" \
    MANGOPAY_BASE_URL="https://api.mangopay.com" \
    MANGOPAY_CLIENT_ID="" \
    MANGOPAY_CLIENT_PASSWORD_FILE="/secrets/mangopay-client-password" \
    CATALOGUE_BASE_URL="http://catalogueapi:8000/" \
    INGEST_BASE_URL="http://ingest:8000/" \
    TRANSFORM_BASE_URL="http://transform:8000/" \
    MAILER_BASE_URL="http://mailer:8000/" \
    MESSENGER_BASE_URL="http://messenger:8000/" \
    RATING_BASE_URL="http://rating:8000/" \
    RATING_USERNAME="" \
    RATING_PASSWORD_FILE="/secrets/rating-password" \
    PROFILE_BASE_URL="http://profile:8000/" \
    PID_BASE_URL="http://pid:8000/"

VOLUME [ \
    "/var/local/opertusmundi/files/assets", \
    "/var/local/opertusmundi/files/users", \
    "/var/local/opertusmundi/files/drafts", \
    "/var/local/opertusmundi/files/temp", \
    "/var/local/opertusmundi/ingest/input" ]

USER spring
ENTRYPOINT [ "/usr/local/bin/docker-entrypoint.sh" ]

