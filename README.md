# API Gateway

[![Build Status](https://ci.dev-1.opertusmundi.eu:9443/api/badges/OpertusMundi/api-gateway-service/status.svg?ref=refs/heads/master)](https://ci.dev-1.opertusmundi.eu:9443/OpertusMundi/api-gateway-service)

Backend for OpertusMundi marketplace frontend

## Quickstart

### Configure the Web Application

Copy configuration example files from `api-gateway/config-example/` into `api-gateway/src/main/resources/`, and edit to adjust to your needs.

`cp -r api-gateway/config-example/* api-gateway/src/main/resources/`

### Database configuration

Set the database configuration properties for all profile configuration files.

* application-development.properties
* application-production.properties

```properties
#
# Data source
#

spring.datasource.url = jdbc:postgresql://localhost:5432/opertus-mundi
spring.datasource.username = username
spring.datasource.password = password
spring.datasource.driver-class-name = org.postgresql.Driver

#
# Logging with a log4j2 JDBC appender
#

opertus-mundi.logging.jdbc.url = jdbc:postgresql://localhost:5432/opertus-mundi
opertus-mundi.logging.jdbc.username = username
opertus-mundi.logging.jdbc.password = password
```

* application-testing.properties

```properties
#
# Data source
#

spring.datasource.url = jdbc:postgresql://localhost:5432/opertus-mundi-test
spring.datasource.username = username
spring.datasource.password = password
spring.datasource.driver-class-name = org.postgresql.Driver
```

### Configure authentication providers

OpertusMundi supports the following authentication methods:

| Provider    | Key           | Description                                          |
| ----------- | ------------- |----------------------------------------------------- |
| API Gateway | forms         | Forms login using username/password                  |
| Google      | google        | OAuth using Google                                   |
| GitHub      | github        | OAuth using GitHub                                   |
| Keycloak    | opertusmundi  | OAuth/OpenID Connect using OpertusMundi Keycloak IDP |

```properties
# Authentication Providers (comma-separated list of authentication provider keys)
opertus-mundi.authentication-providers = forms
```

If an OAuth provider is enabled, the appropriate configuration properties must also be set.

```properties
# Google
github.client.clientId =
github.client.clientSecret =

# GitHub
google.client.clientId =
google.client.clientSecret =

# OpertusMundi
opertus-mundi.client.clientId = 
opertus-mundi.client.clientSecret = 
opertus-mundi.client.preEstablishedRedirectUri =
```

### Configure Feign clients

API Gateway is using [Feign](https://cloud.spring.io/spring-cloud-openfeign/reference/html/) clients for connecting to other system services. For each service, an endpoint must be set and optionally security must be configured.

```properties
#
# Spring Cloud Feign clients
#

# Global secret for signing JWT tokens shared by all services
opertusmundi.feign.jwt.secret=

# Catalogue service (no authentication)
opertusmundi.feign.catalogue.url=

# BPM server (basic authentication)
opertusmundi.feign.bpm-server.url=
opertusmundi.feign.bpm-server.basic-auth.username=
opertusmundi.feign.bpm-server.basic-auth.password=

# Rating service (basic authentication)
opertusmundi.feign.rating-service.url=
opertusmundi.feign.rating-service.basic-auth.username=
opertusmundi.feign.rating-service.basic-auth.password=

# Email service (JWT token authentication)
# Uses private/public key pair for signing/parsing tokens.
opertusmundi.feign.email-service.url=

# Message service (JWT token authentication)
# Uses opertusmundi.feign.jwt.secret for signing tokens.
opertusmundi.feign.message-service.url=

# Ingest service
opertusmundi.feign.ingest.url=

# Transform service
opertusmundi.feign.transform.url=

# Data Profiler service
opertusmundi.feign.data-profiler.url=

# Persistent Identifier Service
opertusmundi.feign.persistent-identifier-service.url=
```

### Configure file system

API Gateway application requires access to asset repository and user file system. The following directories must be accessible to the application:

```properties
#
# File system
#

# Folder for creating temporary files
opertusmundi.file-system.temp-dir=
# Root folder for storing user file system
opertusmundi.file-system.data-dir=
# Root folder for storing draft files
opertusmundi.file-system.draft-dir=
# Root folder for storing asset files
opertusmundi.file-system.asset-dir=
# Root folder for contracts
opertusmundi.file-system.contract-dir=
```

### Configure Payment service

API Gateway implements payments using the MANGOPAY payment solution.

```properties
#
# MangoPay
#

opertusmundi.payments.mangopay.client-id=
opertusmundi.payments.mangopay.client-password=
```

### Configure the Web Client

Details on configuring and running the web client application can be found [here](https://github.com/OpertusMundi/frontend-marketplace).

### Build

Build the project:

`mvn clean package`

### Run as standalone JAR

Run application (with an embedded Tomcat 9.x server) as a standalone application:

`java -jar api-gateway/target/opertus-mundi-api-gateway-1.0.0.jar`

or using the Spring Boot plugin:

`cd api-gateway && mvn spring-boot:run`

### Run as WAR on a servlet container

Normally a WAR archive can be deployed at any servlet container. The following is only tested on a Tomcat 9.x.

Open `pom.xml` and change packaging type to `war`, in order to produce a WAR archive.

Ensure that the following section is not commented (to avoid packaging an embedded server):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
</dependency>    
```

Rebuild, and deploy generated `target/opertus-mundi-api-gateway-1.0.0.war` on a Tomcat 9.x servlet container.

### Endpoints

You can browse the full OpenAPI documentation [here](https://opertusmundi.github.io/api-gateway-service/).
