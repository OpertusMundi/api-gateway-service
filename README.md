# API Gateway

Backend for OpertusMundi marketplace frontend

## Quickstart

### Configure the Web Application

Copy configuration example files from `config-example/` into `src/main/resources/`, and edit to adjust to your needs.

`cp -r config-example/* src/main/resources/`

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

| Provider    | Key           | Description                                         |
| ----------- | ------------- |---------------------------------------------------- |
| API Gateway | forms         | Forms login using username/password                 |
| Google      | google        | OAuth using Google                                  |
| GitHub      | github        | OAuth using GitHub                                  |
| Keycloak    | opertusmundi  |OAuth/OpenID Connect using OpertusMundi Keycloak IDP |

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

### Configure keystore

Email service requires a private/public key pair for signing/parsing JWT tokens. Create a new store in folder `srs/main/resources/jwt`

```bash
keytool -genkey \
        -alias email-service \
        -keystore jwt_keystore \
        -storetype PKCS12 \
        -keyalg RSA  \
        -storepass password \
        -keysize 4096 
```

### Configure Feign clients

API Gateway is using [Feign](https://cloud.spring.io/spring-cloud-openfeign/reference/html/) clients for connecting to other system services. For each service, an endpoint must be set and optionally security must be configured.

```properties
#
# Spring Cloud Feign clients
#

# Path to the key store with private keys for signing JWT tokens
opertusmundi.feign.key-store.path=classpath:jwt/jwt_keystore
# Password for the key store
opertusmundi.feign.key-store.password=password
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
```

### Configure the Web Client

Details on configuring and running the web client application can be found [here](src/main/frontend/README.md).

### Build

Build the project:

`mvn clean package`

### Run as standalone JAR

Run application (with an embedded Tomcat 9.x server) as a standalone application:

`java -jar target/opertus-mundi-api-gateway-1.0.0.jar`

or using the Spring Boot plugin:

`mvn spring-boot:run`

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