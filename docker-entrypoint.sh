#!/bin/sh
set -u -e -o pipefail

[[ "${DEBUG:-false}" != "false" || "${XTRACE:-false}" != "false" ]] && set -x

function _validate_http_url()
{
    local var_name=$1
    local re="^\(https\|http\)://\([a-z][-a-z0-9]*\)\([.][a-z][-a-z0-9]*\)*\([:][1-9][0-9]\{1,4\}\)\?\(/\|$\)"
    grep -e "${re}" || { echo "${var_name} does not seem like an http(s) URL" 1>&2 && false; }
}

function _validate_database_url()
{
    local var_name=$1
    local re="^jdbc:postgresql://\([a-z][-a-z0-9]*\)\([.][a-z][-a-z0-9]*\)*\([:][1-9][0-9]\{1,4\}\)\?/[a-z][-_a-zA-Z0-9]*$"
    grep -e "${re}" || { echo "${var_name} does not seem like a PostgreSQL JDBC connection URL" 1>&2 && false; }
}

runtime_profile=$(hostname | md5sum | head -c10)

{
    public_url=$(echo ${PUBLIC_URL} | _validate_http_url "PUBLIC_URL")
    echo "opertus-mundi.base-url = ${public_url}"

    echo "opertus-mundi.security.csrf-enabled = ${SECURITY_CSRF_ENABLED:-true}"

    database_url=$(echo ${DATABASE_URL} | _validate_database_url "DATABASE_URL")
    database_username=${DATABASE_USERNAME}
    database_password=$(cat ${DATABASE_PASSWORD_FILE} | tr -d '\n')
    echo "spring.datasource.url = ${database_url}"
    echo "spring.datasource.username = ${database_username}"
    echo "spring.datasource.password = ${database_password}"

    echo "opertus-mundi.logging.jdbc.url = ${database_url}"
    echo "opertus-mundi.logging.jdbc.username = ${database_username}"
    echo "opertus-mundi.logging.jdbc.password = ${database_password}"

    jwt_secret=$(cat ${JWT_SECRET_FILE} | tr -d '\n')
    echo "opertusmundi.feign.jwt.secret = ${jwt_secret}"

    if [ -n "${OIDC_AUTH_URL}" ]; then
        oidc_auth_url=$(echo ${OIDC_AUTH_URL} | _validate_http_url "OIDC_AUTH_URL")
        oidc_token_url=$(echo ${OIDC_TOKEN_URL} | _validate_http_url "OIDC_TOKEN_URL")
        oidc_userinfo_url=$(echo ${OIDC_USERINFO_URL} | _validate_http_url "OIDC_USERINFO_URL")
        oidc_jwks_url=$(echo ${OIDC_JWKS_URL} | _validate_http_url "OIDC_JWKS_URL")
        oidc_scope=${OIDC_SCOPE:-openid}
        oidc_client_id=${OIDC_CLIENT_ID}
        oidc_client_secret=$(cat ${OIDC_CLIENT_SECRET_FILE} | tr -d '\n')
        # See https://github.com/OpertusMundi/api-gateway-service/blob/26a5b59baa2bb03a38fc324e336f1a2689c662be/api-gateway/config-example/config/application.properties#L58
        echo "opertus-mundi.authentication-providers = opertusmundi"
        echo "opertus-mundi.client.clientId = ${oidc_client_id}"
        echo "opertus-mundi.client.clientSecret = ${oidc_client_secret}"
        echo "opertus-mundi.client.accessTokenUri = ${oidc_token_url}"
        echo "opertus-mundi.client.userAuthorizationUri = ${oidc_auth_url}"
        echo "opertus-mundi.client.useCurrentUri = false"
        echo "opertus-mundi.client.preEstablishedRedirectUri ="
        echo "opertus-mundi.client.scope = ${oidc_scope}"
        echo "opertus-mundi.user-info-endpoint = ${oidc_userinfo_url}"
        echo "opertus-mundi.jwks-uri = ${oidc_jwks_url}"
    fi

    bpm_rest_base_url=$(echo ${BPM_REST_BASE_URL} | _validate_http_url "BPM_REST_BASE_URL")
    bpm_rest_username=${BPM_REST_USERNAME}
    bpm_rest_password=$(cat ${BPM_REST_PASSWORD_FILE} | tr -d '\n')
    echo "opertusmundi.feign.bpm-server.url = ${bpm_rest_base_url}"
    echo "opertusmundi.feign.bpm-server.basic-auth.username = ${bpm_rest_username}"
    echo "opertusmundi.feign.bpm-server.basic-auth.password = ${bpm_rest_password}"

    mangopay_base_url=$(echo ${MANGOPAY_BASE_URL} | _validate_http_url "MANGOPAY_BASE_URL")
    mangopay_client_id=${MANGOPAY_CLIENT_ID}
    mangopay_client_password=$(cat ${MANGOPAY_CLIENT_PASSWORD_FILE} | tr -d '\n')
    echo "opertusmundi.payments.mangopay.base-url = ${mangopay_base_url}"
    echo "opertusmundi.payments.mangopay.client-id = ${mangopay_client_id}"
    echo "opertusmundi.payments.mangopay.client-password = ${mangopay_client_password}"
    echo "opertusmundi.payments.mangopay.secure-mode-return-url = ${public_url}"

    catalogue_base_url=$(echo ${CATALOGUE_BASE_URL} | _validate_http_url "CATALOGUE_BASE_URL")
    echo "opertusmundi.feign.catalogue.url = ${catalogue_base_url}"

    ingest_base_url=$(echo ${INGEST_BASE_URL} | _validate_http_url "INGEST_BASE_URL")
    echo "opertusmundi.feign.ingest.url = ${ingest_base_url}"

    transform_base_url=$(echo ${TRANSFORM_BASE_URL} | _validate_http_url "TRANSFORM_BASE_URL")
    echo "opertusmundi.feign.transform.url = ${transform_base_url}"

    mailer_base_url=$(echo ${MAILER_BASE_URL} | _validate_http_url "MAILER_BASE_URL")
    echo "opertusmundi.feign.email-service.url = ${mailer_base_url}"
    echo "opertusmundi.feign.email-service.jwt.subject = api-gateway"

    messenger_base_url=$(echo ${MESSENGER_BASE_URL} | _validate_http_url "MESSENGER_BASE_URL")
    echo "opertusmundi.feign.message-service.url = ${messenger_base_url}"
    echo "opertusmundi.feign.message-service.jwt.subject = api-gateway"

    rating_base_url=$(test -z "${RATING_BASE_URL:-}" && echo -n || \
        { echo ${RATING_BASE_URL} | _validate_http_url "RATING_BASE_URL"; })
    rating_username=${RATING_USERNAME}
    rating_password=$({ test -f "${RATING_PASSWORD_FILE}" && cat ${RATING_PASSWORD_FILE}; } | tr -d '\n' || echo -n)
    echo "opertusmundi.feign.rating-service.url = ${rating_base_url}"
    echo "opertusmundi.feign.rating-service.basic-auth.username = ${rating_username}"
    echo "opertusmundi.feign.rating-service.basic-auth.password = ${rating_password}"

    profile_base_url=$(echo ${PROFILE_BASE_URL} | _validate_http_url "PROFILE_BASE_URL")
    echo "opertusmundi.feign.data-profiler.url = ${profile_base_url}"
    
    pid_base_url=$(echo ${PID_BASE_URL} | _validate_http_url "PID_BASE_URL")
    echo "opertusmundi.feign.persistent-identifier-service.url= ${pid_base_url}"

    elasticsearch_base_url=$(echo ${ELASTICSEARCH_BASE_URL%/} | _validate_http_url "ELASTICSEARCH_BASE_URL")
    elasticsearch_indices_assets_index_name=${ELASTICSEARCH_INDICES_ASSETS_INDEX_NAME}
    elasticsearch_indices_assets_view_index_name=${ELASTICSEARCH_INDICES_ASSETS_VIEW_INDEX_NAME}
    elasticsearch_indices_assets_view_aggregate_index_name=${ELASTICSEARCH_INDICES_ASSETS_VIEW_AGGREGATE_INDEX_NAME}
    elasticsearch_indices_profiles_index_name=${ELASTICSEARCH_INDICES_PROFILES_INDEX_NAME}
    echo "spring.elasticsearch.rest.uris = ${elasticsearch_base_url}"
    echo "opertusmundi.elastic.asset-index.name = ${elasticsearch_indices_assets_index_name}"
    echo "opertusmundi.elastic.asset-view-index.name = ${elasticsearch_indices_assets_view_index_name}"
    echo "opertusmundi.elastic.asset-view-aggregate-index.name = ${elasticsearch_indices_assets_view_aggregate_index_name}"
    echo "opertusmundi.elastic.profile-index.name = ${elasticsearch_indices_profiles_index_name}"
    
    jupyterhub_url=$(echo ${JUPYTERHUB_URL%/} | _validate_http_url "JUPYTERHUB_URL")
    jupyterhub_api_url=$(echo ${JUPYTERHUB_API_URL%/} | _validate_http_url "JUPYTERHUB_API_URL")
    jupyterhub_api_key=$(cat ${JUPYTERHUB_API_KEY_FILE} | tr -d '\n')
    echo "opertusmundi.jupyterhub.url = ${jupyterhub_url}"
    echo "opertusmundi.feign.jupyterhub.url = ${jupyterhub_api_url}"
    echo "opertusmundi.feign.jupyterhub.access-token = ${jupyterhub_api_key}"

    geoserver_base_url=$(echo ${GEOSERVER_BASE_URL%/} | _validate_http_url "GEOSERVER_BASE_URL")
    echo "opertusmundi.geoserver.endpoint = ${geoserver_base_url}"

    echo "opertusmundi.googleanalytics.tracker-id = ${GOOGLEANALYTICS_TRACKER_ID:-}"

    wordpress_base_url=$(echo ${WORDPRESS_BASE_URL} | _validate_http_url "WORDPRESS_BASE_URL")
    echo "opertus-mundi.wordpress.endpoint = ${wordpress_base_url}"
    
    sentinelhub_enabled=${SENTINELHUB_ENABLED:-false}
    sentinelhub_client_id=${SENTINELHUB_CLIENT_ID}
    sentinelhub_client_secret=$(test -n "${SENTINELHUB_CLIENT_ID}" && cat ${SENTINELHUB_CLIENT_SECRET_FILE} | tr -d '\n')
    echo "opertusmundi.sentinel-hub.enabled = ${sentinelhub_enabled}"
    echo "opertusmundi.sentinel-hub.client-id = ${sentinelhub_client_id}" 
    echo "opertusmundi.sentinel-hub.client-secret = ${sentinelhub_client_secret}"

} > ./config/application-${runtime_profile}.properties

logging_config="classpath:config/log4j2.xml"
if [[ -f "./config/log4j2.xml" ]]; then
    logging_config="file:config/log4j2.xml"
fi

# Run

main_class=eu.opertusmundi.web.Application
default_java_opts="-server -Djava.security.egd=file:///dev/urandom -Xms256m"
exec java ${JAVA_OPTS:-${default_java_opts}} -cp "/app/classes:/app/dependency/*" \
  -Dspring.profiles.active=production,${runtime_profile} -Dlogging.config=${logging_config} \
  ${main_class}

