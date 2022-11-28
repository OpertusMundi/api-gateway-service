#!/bin/bash
set -u -e -o pipefail

[[ "${DEBUG:-false}" != "false" || "${XTRACE:-false}" != "false" ]] && set -x

function _validate_http_url()
{
    local var_name=$1
    local re="^\(https\|http\)://\([a-z0-9][-a-z0-9]*\)\([.][a-z0-9][-a-z0-9]*\)*\([:][1-9][0-9]\{1,4\}\)\?\(/\|$\)"
    grep -e "${re}" || { echo "${var_name} does not seem like an http(s) URL" 1>&2 && false; }
}

function _validate_database_url()
{
    local var_name=$1
    local re="^jdbc:postgresql://\([a-z0-9][-a-z0-9]*\)\([.][a-z0-9][-a-z0-9]*\)*\([:][1-9][0-9]\{1,4\}\)\?/[a-z][-_a-zA-Z0-9]*$"
    grep -e "${re}" || { echo "${var_name} does not seem like a PostgreSQL JDBC connection URL" 1>&2 && false; }
}

# Generate application properties

runtime_profile=$(hostname | md5sum | head -c10)

{
    public_url=$(echo ${PUBLIC_URL} | _validate_http_url "PUBLIC_URL")
    echo "opertus-mundi.base-url = ${public_url}"

    echo "opertus-mundi.security.csrf-enabled = ${SECURITY_CSRF_ENABLED:-true}"
    
    servlet_multipart_max_request_size=$(echo ${SERVLET_MULTIPART_MAX_REQUEST_SIZE} | tr '[:lower:]' '[:upper:]' | grep -E -e '^([1-9][0-9]*)MB$')
    servlet_multipart_max_request_size_megabytes=${servlet_multipart_max_request_size%MB}
    [[ ${servlet_multipart_max_request_size_megabytes} -gt "8" ]] && [[ ${servlet_multipart_max_request_size_megabytes} -lt "100" ]]
    echo "spring.servlet.multipart.max-file-size = ${servlet_multipart_max_request_size_megabytes}MB"
    echo "spring.servlet.multipart.max-request-size = ${servlet_multipart_max_request_size_megabytes}MB"

    database_url=$(echo ${DATABASE_URL} | _validate_database_url "DATABASE_URL")
    database_username=${DATABASE_USERNAME}
    database_password=$(cat ${DATABASE_PASSWORD_FILE} | tr -d '\n')
    echo "spring.datasource.url = ${database_url}"
    echo "spring.datasource.username = ${database_username}"
    echo "spring.datasource.password = ${database_password}"

    jwt_secret=$(cat ${JWT_SECRET_FILE} | tr -d '\n')
    echo "opertusmundi.feign.jwt.secret = ${jwt_secret}"

    authentication_providers=( "forms" )

    if [[ -n "${OIDC_AUTH_URL}" ]]; then
        oidc_auth_url=$(echo ${OIDC_AUTH_URL} | _validate_http_url "OIDC_AUTH_URL")
        oidc_token_url=$(echo ${OIDC_TOKEN_URL} | _validate_http_url "OIDC_TOKEN_URL")
        oidc_userinfo_url=$(echo ${OIDC_USERINFO_URL} | _validate_http_url "OIDC_USERINFO_URL")
        oidc_jwks_url=$(echo ${OIDC_JWKS_URL} | _validate_http_url "OIDC_JWKS_URL")
        oidc_scope=${OIDC_SCOPE:-openid}
        oidc_client_id=${OIDC_CLIENT_ID}
        oidc_client_secret=$(cat ${OIDC_CLIENT_SECRET_FILE} | tr -d '\n')
        authentication_providers+=( "opertusmundi" )
        # https://github.com/OpertusMundi/api-gateway-service/blob/dea7d3d0bd008b2b1b072de8117010b252b2767f/api-gateway/config-example/config/application.properties#L64-L88
        # Define the OAuth2 provider
        echo "spring.security.oauth2.client.provider.keycloak.authorization-uri = ${oidc_auth_url}"
        echo "spring.security.oauth2.client.provider.keycloak.token-uri = ${oidc_token_url}"
        echo "spring.security.oauth2.client.provider.keycloak.jwk-set-uri = ${oidc_jwks_url}"
        echo "spring.security.oauth2.client.provider.keycloak.user-info-uri = ${oidc_userinfo_url}"
        echo "spring.security.oauth2.client.provider.keycloak.user-name-attribute = email"
        # Register OAuth2 client "opertusmundi" for the webapp
        echo "spring.security.oauth2.client.registration.opertusmundi.provider = keycloak"
        echo "spring.security.oauth2.client.registration.opertusmundi.authorization-grant-type = authorization_code"
        echo "spring.security.oauth2.client.registration.opertusmundi.client-id = ${oidc_client_id}"
        echo "spring.security.oauth2.client.registration.opertusmundi.client-secret = ${oidc_client_secret}"
        echo "spring.security.oauth2.client.registration.opertusmundi.redirect-uri = {baseUrl}/login/oauth2/code/{registrationId}"
        echo "spring.security.oauth2.client.registration.opertusmundi.scope = ${oidc_scope}"
        # Register OAuth2 client "opertusmundi-development" for local development of frontend application
        echo "spring.security.oauth2.client.registration.opertusmundi-development.provider = keycloak"
        echo "spring.security.oauth2.client.registration.opertusmundi-development.authorization-grant-type = authorization_code"
        echo "spring.security.oauth2.client.registration.opertusmundi-development.client-id = ${oidc_client_id}"
        echo "spring.security.oauth2.client.registration.opertusmundi-development.client-secret = ${oidc_client_secret}"
        echo "spring.security.oauth2.client.registration.opertusmundi-development.redirect-uri = http://localhost:4200/login/oauth2/code/{registrationId}"
        echo "spring.security.oauth2.client.registration.opertusmundi-development.scope = ${oidc_scope}"
    fi
   
    if [[ -n "${GOOGLE_OIDC_CLIENT_ID}" ]]; then
        authentication_providers+=( "google" )
        google_oidc_client_id=${GOOGLE_OIDC_CLIENT_ID}
        google_oidc_client_secret=$(cat ${GOOGLE_OIDC_CLIENT_SECRET_FILE} | tr -d '\n')
        # Register OAuth2 client "google" for the webapp
        echo "spring.security.oauth2.client.registration.google.client-id = ${google_oidc_client_id}"
        echo "spring.security.oauth2.client.registration.google.client-secret = ${google_oidc_client_secret}"
        echo "spring.security.oauth2.client.registration.google.redirect-uri = {baseUrl}/login/oauth2/code/{registrationId}"
    fi

    if [[ -n "${GITHUB_OIDC_CLIENT_ID}" ]]; then
        authentication_providers+=( "github" )
        github_oidc_client_id=${GITHUB_OIDC_CLIENT_ID}
        github_oidc_client_secret=$(cat ${GITHUB_OIDC_CLIENT_SECRET_FILE} | tr -d '\n')
        # Register OAuth2 client "github" for the webapp
        echo "spring.security.oauth2.client.registration.github.client-id = ${github_oidc_client_id}"
        echo "spring.security.oauth2.client.registration.github.client-secret = ${github_oidc_client_secret}"
        echo "spring.security.oauth2.client.registration.github.redirect-uri = {baseUrl}/login/oauth2/code/{registrationId}"
    fi

    authentication_providers_as_string=$(IFS=','; echo "${authentication_providers[*]}")
    echo "opertus-mundi.authentication-providers = ${authentication_providers_as_string}"

    bpm_rest_base_url=$(echo ${BPM_REST_BASE_URL} | _validate_http_url "BPM_REST_BASE_URL")
    bpm_rest_username=${BPM_REST_USERNAME}
    bpm_rest_password=$(cat ${BPM_REST_PASSWORD_FILE} | tr -d '\n')
    echo "opertusmundi.feign.bpm-server.url = ${bpm_rest_base_url}"
    echo "opertusmundi.feign.bpm-server.basic-auth.username = ${bpm_rest_username}"
    echo "opertusmundi.feign.bpm-server.basic-auth.password = ${bpm_rest_password}"

    mangopay_base_url=$(echo ${MANGOPAY_BASE_URL} | _validate_http_url "MANGOPAY_BASE_URL")
    mangopay_client_id=${MANGOPAY_CLIENT_ID}
    mangopay_client_password=$(cat ${MANGOPAY_CLIENT_PASSWORD_FILE} | tr -d '\n')
    echo "opertusmundi.payments.mangopay.web-hook.create-on-startup = ${MANGOPAY_WEBHOOK_CREATE_ON_STARTUP:-false}"
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

    profile_base_url=$(echo ${PROFILE_BASE_URL} | _validate_http_url "PROFILE_BASE_URL")
    echo "opertusmundi.feign.data-profiler.url = ${profile_base_url}"

    pid_base_url=$(echo ${PID_BASE_URL} | _validate_http_url "PID_BASE_URL")
    echo "opertusmundi.feign.persistent-identifier-service.url= ${pid_base_url}"

    elasticsearch_base_url=$(echo ${ELASTICSEARCH_BASE_URL%/} | _validate_http_url "ELASTICSEARCH_BASE_URL")
    elasticsearch_indices_assets_index_name=${ELASTICSEARCH_INDICES_ASSETS_INDEX_NAME}
    elasticsearch_indices_assets_view_index_name=${ELASTICSEARCH_INDICES_ASSETS_VIEW_INDEX_NAME}
    elasticsearch_indices_assets_view_aggregate_index_name=${ELASTICSEARCH_INDICES_ASSETS_VIEW_AGGREGATE_INDEX_NAME}
    elasticsearch_indices_profiles_index_name=${ELASTICSEARCH_INDICES_PROFILES_INDEX_NAME}
    echo "spring.elasticsearch.uris = ${elasticsearch_base_url}"
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

    echo "opertusmundi.google-analytics.key-file-location = ${GOOGLE_SERVICE_ACCOUNT_KEY_FILE:-}"
    echo "opertusmundi.google-analytics.view-id = ${GOOGLEANALYTICS_VIEW_ID:-}"

    wordpress_base_url=$(echo ${WORDPRESS_BASE_URL} | _validate_http_url "WORDPRESS_BASE_URL")
    echo "opertus-mundi.wordpress.endpoint = ${wordpress_base_url}"

    sentinelhub_enabled=${SENTINELHUB_ENABLED:-false}
    echo "opertusmundi.sentinel-hub.enabled = ${sentinelhub_enabled}"
    sentinelhub_client_id=
    sentinelhub_client_secret=
    if [[ ${sentinelhub_enabled} != "false" ]]; then
        sentinelhub_client_id=${SENTINELHUB_CLIENT_ID}
        sentinelhub_client_secret=$(cat ${SENTINELHUB_CLIENT_SECRET_FILE} | tr -d '\n')
    fi
    echo "opertusmundi.sentinel-hub.client-id = ${sentinelhub_client_id}"
    echo "opertusmundi.sentinel-hub.client-secret = ${sentinelhub_client_secret}"

    contract_signpdf_keystore=$(realpath ${CONTRACT_SIGNPDF_KEYSTORE})
    test -f "${contract_signpdf_keystore}"
    contract_signpdf_keystore_password=$(cat ${CONTRACT_SIGNPDF_KEYSTORE_PASSWORD_FILE} | tr -d '\n')
    contract_signpdf_key_alias=${CONTRACT_SIGNPDF_KEY_ALIAS}
    test -n "${contract_signpdf_key_alias}"
    echo "opertusmundi.contract.signpdf.key-store = file://${contract_signpdf_keystore}"
    echo "opertusmundi.contract.signpdf.key-store-password = ${contract_signpdf_keystore_password}"
    echo "opertusmundi.contract.signpdf.key-alias = ${contract_signpdf_key_alias}"
    
    if [[ -n "${KEYCLOAK_URL}" ]]; then
        keycloak_url=$(echo ${KEYCLOAK_URL} | _validate_http_url "KEYCLOAK_URL")
        keycloak_realm=${KEYCLOAK_REALM}
        keycloak_services_realm=${KEYCLOAK_SERVICES_REALM}
        keycloak_refresh_token=$(cat ${KEYCLOAK_REFRESH_TOKEN_FILE} | tr -d '\n')
        echo "opertusmundi.feign.keycloak.url = ${keycloak_url}"
        echo "opertusmundi.feign.keycloak.realm = ${keycloak_realm}"
        echo "opertusmundi.feign.keycloak.admin.refresh-token.refresh-token = ${keycloak_refresh_token}"
        echo "opertusmundi.account-client-service.keycloak.realm = ${keycloak_services_realm}"
        if [[ -n "${KEYCLOAK_PUBLIC_URL}" ]]; then 
            keycloak_public_url=$(echo ${KEYCLOAK_PUBLIC_URL} | _validate_http_url "KEYCLOAK_PUBLIC_URL")
        else
            keycloak_public_url=${keycloak_url}
        fi
        echo "spring.security.oauth2.resourceserver.jwt.issuer-uri = ${keycloak_public_url%/}/realms/${keycloak_realm}"
    fi 

    cors_allowed_origins=${CORS_ALLOWED_ORIGINS}
    echo "opertusmundi.web.cors.allowed-origins = ${cors_allowed_origins}"

} > ./config/application-${runtime_profile}.properties

# Point to logging configuration

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

