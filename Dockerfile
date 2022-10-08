# vim: set syntax=dockerfile:

FROM node:10.16.3-buster AS npm-build

ENV NPM_CONFIG_PROGRESS="false" \
    NPM_CONFIG_SPIN="false" \
    HUSKY_SKIP_INSTALL="true"

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN apt-get update && apt-get install -y --no-install-recommends jq moreutils \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY api-gateway/src/main/frontend /app/
RUN jq -f ./filter-deps-for-prod.jq package.json | sponge package.json
RUN npm install && npm run build


#FROM maven:3.8.6-eclipse-temurin-17-alpine as maven-build
# see https://github.com/OpertusMundi/java-commons/blob/master/Dockerfile
FROM opertusmundi/java-commons-builder:1.1 as maven-build

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


FROM eclipse-temurin:17-jre-alpine 

ARG git_url=
ARG git_commit=
ARG git_tags=
ARG git_build_time=

SHELL ["/bin/ash", "-o", "pipefail", "-c"]

COPY --from=maven-build /app/api-gateway/target/ /app/

RUN apk update && apk add --no-cache bash
RUN addgroup spring && adduser -H -D -G spring spring

COPY docker-entrypoint.sh /usr/local/bin
RUN chmod a+x /usr/local/bin/docker-entrypoint.sh

WORKDIR /app

RUN mkdir config logs \
    && chgrp spring config logs \
    && chmod g=rwx config logs

ENV PUBLIC_URL="" \
    SERVLET_MULTIPART_MAX_REQUEST_SIZE="20MB" \
    SECURITY_CSRF_ENABLED="true" \
    DATABASE_URL="jdbc:postgresql://db:5432/opertusmundi" \
    DATABASE_USERNAME="spring" \
    DATABASE_PASSWORD_FILE="/secrets/database-password" \
    JWT_SECRET_FILE="/secrets/jwt-signing-key" \
    OIDC_CLIENT_ID="api-gateway" \
    OIDC_CLIENT_SECRET_FILE="/secrets/openid-client-secret" \
    OIDC_SCOPE="openid,email,profile,roles" \
    OIDC_AUTH_URL="" \
    OIDC_TOKEN_URL="" \
    OIDC_USERINFO_URL="" \
    OIDC_JWKS_URL="" \
    GOOGLE_OIDC_CLIENT_ID="" \
    GOOGLE_OIDC_CLIENT_SECRET_FILE="" \
    GITHUB_OIDC_CLIENT_ID="" \
    GITHUB_OIDC_CLIENT_SECRET_FILE="" \
    BPM_REST_BASE_URL="http://bpm-server:8000/engine-rest" \
    BPM_REST_USERNAME="" \
    BPM_REST_PASSWORD_FILE="/secrets/bpm-rest-password" \
    MANGOPAY_WEBHOOK_CREATE_ON_STARTUP="false" \
    MANGOPAY_BASE_URL="https://api.mangopay.com" \
    MANGOPAY_CLIENT_ID="" \
    MANGOPAY_CLIENT_PASSWORD_FILE="/secrets/mangopay-client-password" \
    CATALOGUE_BASE_URL="http://catalogueapi:8000/" \
    INGEST_BASE_URL="http://ingest:8000/" \
    TRANSFORM_BASE_URL="http://transform:8000/" \
    MAILER_BASE_URL="http://mailer:8000/" \
    MESSENGER_BASE_URL="http://messenger:8000/" \
    PROFILE_BASE_URL="http://profile:8000/" \
    PID_BASE_URL="http://pid:8000/" \
    ELASTICSEARCH_BASE_URL="http://elasticsearch:9200" \
    ELASTICSEARCH_INDICES_ASSETS_INDEX_NAME="assets" \
    ELASTICSEARCH_INDICES_ASSETS_VIEW_INDEX_NAME="assets_view" \
    ELASTICSEARCH_INDICES_ASSETS_VIEW_AGGREGATE_INDEX_NAME="assets_view_aggregate" \
    ELASTICSEARCH_INDICES_PROFILES_INDEX_NAME="profiles" \
    JUPYTERHUB_URL="http://jupyterhub/" \
    JUPYTERHUB_API_URL="http://jupyterhub/hub/api" \
    JUPYTERHUB_API_KEY_FILE="/secrets/jupyterhub-access-token" \
    GEOSERVER_BASE_URL="http://geoserver:8080/geoserver" \
    GOOGLE_SERVICE_ACCOUNT_KEY_FILE="" \
    GOOGLEANALYTICS_VIEW_ID="" \
    WORDPRESS_BASE_URL="https://posts.opertusmundi.eu" \
    SENTINELHUB_ENABLED="false" \
    SENTINELHUB_CLIENT_ID="" \
    SENTINELHUB_CLIENT_SECRET_FILE="/secrets/sentinelhub-client-secret" \
    CONTRACT_SIGNPDF_KEYSTORE="/secrets/signatory-keystore" \
    CONTRACT_SIGNPDF_KEYSTORE_PASSWORD_FILE="/secrets/signatory-keystore-password" \
    CONTRACT_SIGNPDF_KEY_ALIAS="opertusmundi.eu"


ENV GIT_URL="${git_url}" \
    GIT_COMMIT="${git_commit}" \
    GIT_TAGS="${git_tags}" \
    GIT_BUILD_TIME="${git_build_time}"

RUN /bin/echo -e \
    "git.remote.origin.url=${GIT_URL}" \
    "\ngit.commit.id=${GIT_COMMIT}" \
    "\ngit.commit.id.full=${GIT_COMMIT}" \
    "\ngit.commit.id.abbrev=${GIT_COMMIT:0:7}" \
    "\ngit.commit.id.describe=${GIT_TAGS}" \
    "\ngit.tags=${GIT_TAGS}" \
    "\ngit.build.time=${GIT_BUILD_TIME}" \
  | sed -E -e 's/[[:space:]]+$//' | tee classes/git.properties 

VOLUME [ \
    "/var/local/opertusmundi/files/assets", \
    "/var/local/opertusmundi/files/users", \
    "/var/local/opertusmundi/files/drafts", \
    "/var/local/opertusmundi/files/temp", \
    "/var/local/opertusmundi/ingest/input" ]

USER spring
ENTRYPOINT [ "/usr/local/bin/docker-entrypoint.sh" ]

