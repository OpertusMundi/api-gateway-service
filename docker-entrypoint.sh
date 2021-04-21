#!/bin/sh
set -u -e -o pipefail

[[ "${DEBUG:-f}" != "f" || "${XTRACE:-f}" != "f" ]] && set -x

function _validate_http_url()
{
    local var_name=$1
    local pattern="^\(https\|http\):[/][/]\([a-z][-a-z0-9]*\)\([.][a-z][-a-z0-9]*\)*\([:][1-9][0-9]\{1,4\}\)\?\([/]\|$\)"
    grep -e "${pattern}" || { echo "${var_name} does not seem like an http(s) URL" 1>&2 && false; }
}

function _validate_database_url()
{
    local var_name=$1
    local pattern="^jdbc:postgresql:[/][/]\([a-z][-a-z0-9]*\)\([.][a-z][-a-z0-9]*\)*\([:][1-9][0-9]\{1,4\}\)\?[/][a-z][-_a-zA-Z0-9]*$"
    grep -e "${pattern}" || { echo "${var_name} does not seem like a PostgreSQL JDBC connection URL" 1>&2 && false; }
}

function _generate_configuration_for_self()
{
    base_url=$(echo ${BASE_URL} | _validate_http_url "BASE_URL")
    echo "opertus-mundi.base-url = ${base_url}"
}

function _generate_configuration_for_datasource()
{
    database_url=$(echo ${DATABASE_URL} | _validate_database_url "DATABASE_URL")
    database_username=${DATABASE_USERNAME}
    database_password=$(cat ${DATABASE_PASSWORD_FILE} | tr -d '\n')
    
    cat <<-EOD
	spring.datasource.url = ${database_url}
	spring.datasource.username = ${database_username}
	spring.datasource.password = ${database_password}
	EOD
}

function _generate_configuration_for_clients()
{
    jwt_secret=$(cat ${JWT_SECRET_FILE} | tr -d '\n')
    
    bpm_rest_base_url=$(echo ${BPM_REST_BASE_URL} | _validate_http_url "BPM_REST_BASE_URL")
    bpm_rest_username=${BPM_REST_USERNAME}
    bpm_rest_password=$(cat ${BPM_REST_PASSWORD_FILE} | tr -d '\n')

    mangopay_base_url=$(echo ${MANGOPAY_BASE_URL} | _validate_http_url "MANGOPAY_BASE_URL")
    mangopay_client_id=${MANGOPAY_CLIENT_ID}
    mangopay_client_password=$(cat ${MANGOPAY_CLIENT_PASSWORD_FILE} | tr -d '\n')

    catalogue_base_url=$(echo ${CATALOGUE_BASE_URL} | _validate_http_url "CATALOGUE_BASE_URL")

    ingest_base_url=$(echo ${INGEST_BASE_URL} | _validate_http_url "INGEST_BASE_URL")

    transform_base_url=$(echo ${TRANSFORM_BASE_URL} | _validate_http_url "TRANSFORM_BASE_URL")
   
    mailer_base_url=$(echo ${MAILER_BASE_URL} | _validate_http_url "MAILER_BASE_URL")
    
    messenger_base_url=$(echo ${MESSENGER_BASE_URL} | _validate_http_url "MESSENGER_BASE_URL")

    rating_base_url=$(test -z "${RATING_BASE_URL:-}" && echo -n || \
        { echo ${RATING_BASE_URL} | _validate_http_url "RATING_BASE_URL"; })
    rating_username=${RATING_USERNAME}
    rating_password=$({ test -f "${RATING_PASSWORD_FILE}" && \
        cat ${RATING_PASSWORD_FILE}; } | tr -d '\n' || echo -n)

    profile_base_url=$(echo ${PROFILE_BASE_URL} | _validate_http_url "PROFILE_BASE_URL")
    
    pid_base_url=$(echo ${PID_BASE_URL} | _validate_http_url "PID_BASE_URL")

    cat <<-EOD
	opertusmundi.feign.jwt.secret = ${jwt_secret}
	opertusmundi.payments.mangopay.base-url = ${mangopay_base_url}
	opertusmundi.payments.mangopay.client-id = ${mangopay_client_id}
	opertusmundi.payments.mangopay.client-password = ${mangopay_client_password}
	opertusmundi.feign.bpm-server.url = ${bpm_rest_base_url}
	opertusmundi.feign.bpm-server.basic-auth.username = ${bpm_rest_username}
	opertusmundi.feign.bpm-server.basic-auth.password = ${bpm_rest_password}
	opertusmundi.feign.catalogue.url = ${catalogue_base_url}
	opertusmundi.feign.ingest.url = ${ingest_base_url}
	opertusmundi.feign.transform.url = ${transform_base_url}
	opertusmundi.feign.email-service.url = ${mailer_base_url}
	opertusmundi.feign.email-service.jwt.subject = api-gateway
	opertusmundi.feign.message-service.url = ${messenger_base_url}
	opertusmundi.feign.message-service.jwt.subject = api-gateway
	opertusmundi.feign.rating-service.url = ${rating_base_url}
	opertusmundi.feign.rating-service.basic-auth.username = ${rating_username}
	opertusmundi.feign.rating-service.basic-auth.password = ${rating_password}
	opertusmundi.feign.data-profiler.url = ${profile_base_url}
	opertusmundi.feign.persistent-identifier-service.url= ${pid_base_url}
	EOD
}

# Generate configuration from environment

runtime_profile=$(hostname | md5sum | head -c 32)

{
    _generate_configuration_for_self;    
    _generate_configuration_for_datasource; 
    _generate_configuration_for_clients;
} >./config/application-${runtime_profile}.properties

# Run

main_class=eu.opertusmundi.web.Application
default_java_opts="-server -Djava.security.egd=file:///dev/urandom -Xms256m"
exec java ${JAVA_OPTS:-${default_java_opts}} -cp "/app/classes:/app/dependency/*" ${main_class} \
  --spring.profiles.active=production,${runtime_profile}

