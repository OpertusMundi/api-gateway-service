package eu.opertusmundi.test.support.integration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.StreamSupport;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import eu.opertusmundi.test.support.utils.ResponsePayload;

@ActiveProfiles("testing")
@TestInstance(Lifecycle.PER_CLASS)
@Testcontainers
public class BaseIntegrationTest {

    private static final String   KEYCLOAK_IMAGE        = "quay.io/keycloak/keycloak:20.0.1";
    private static final String   KEYCLOAK_ADMIN_REALM  = "master";
    private static final String   KEYCLOAK_ADMIN_CLIENT = "admin-cli";
    protected static final String KEYCLOAK_TOPIO_REALM  = "topio";
    protected static final String KEYCLOAK_TOPIO_CLIENT = "topio-marketplace";

    protected static final String TOPIO_USERNAME = "user@opertusmundi.eu";
    protected static final String TOPIO_PASSWORD = "password";

    private static final String POSTGIS_IMAGE_TAG = "10-2.5-alpine";
    private static final String DB_NAME           = "opertus-mundi";
    private static final String DB_USERNAME       = "dev";
    private static final String DB_PASSWORD       = "dev";

    /**
     * Container initialization
     */

    protected static final JdbcDatabaseContainer<?> postgisContainer = new PostgisContainerProvider()
        .newInstance(POSTGIS_IMAGE_TAG)
        .withDatabaseName(DB_NAME)
        .withUsername(DB_USERNAME)
        .withPassword(DB_PASSWORD);

    protected static final KeycloakContainer keycloakContainer = new KeycloakContainer(KEYCLOAK_IMAGE)
        .withAdminUsername("admin")
        .withAdminPassword("admin")
        .withRealmImportFile("/keycloak/topio-realm.json");

    static {
        postgisContainer.start();
        keycloakContainer.start();
    }

    @DynamicPropertySource
    private static void setDynamicProperties(DynamicPropertyRegistry registry) throws IOException {
        registry.add("spring.datasource.url", postgisContainer::getJdbcUrl);
        registry.add("spring.datasource.username", () -> DB_USERNAME);
        registry.add("spring.datasource.password", () -> DB_PASSWORD);

        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.baseline-version", () -> "0_0_0");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
        registry.add("spring.flyway.locations", () -> "filesystem:${application.parent.project.base-dir}/cli/src/main/resources/db/migration");
        registry.add("spring.flyway.schemas", () -> "public,web");
        registry.add("spring.flyway.table", () -> "db_version");

        final String authServerUrl = keycloakContainer.getAuthServerUrl();
        final String refreshToken  = getRefreshToken();

        registry.add("opertusmundi.feign.keycloak.url", () -> keycloakContainer.getAuthServerUrl());
        registry.add("opertusmundi.feign.keycloak.realm", () -> KEYCLOAK_TOPIO_REALM);
        registry.add("opertusmundi.feign.keycloak.admin.refresh-token.refresh-token", () -> refreshToken);
        registry.add("opertusmundi.feign.keycloak.admin.refresh-token.retry.backoff.initial-interval-millis", () -> 2000);
        registry.add("opertusmundi.feign.keycloak.admin.refresh-token.retry.backoff.multiplier", () -> 2.0);
        registry.add("opertusmundi.feign.keycloak.admin.refresh-token.retry.backoff.max-interval-millis", () -> 360000);
        registry.add("opertusmundi.feign.keycloak.admin.refresh-token.retry.backoff.max", () -> 5);
        registry.add("opertusmundi.feign.keycloak.log-level", () -> "NONE");

        registry.add("opertusmundi.account-client-service.keycloak.realm", () -> "${opertusmundi.feign.keycloak.realm}");

        final String issuerUri = authServerUrl + "realms/" + KEYCLOAK_TOPIO_REALM;
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuerUri);

        final var       realmConfig  = ResponsePayload.from("classpath:keycloak/topio-realm.json");
        final ArrayNode clients      = (ArrayNode) realmConfig.asJson().get("clients");
        final String    clientSecret = StreamSupport.stream(clients.spliterator(), false)
            .filter(c -> c.get("clientId").asText().equals(KEYCLOAK_TOPIO_CLIENT))
            .map(c -> c.get("secret").asText())
            .findFirst()
            .orElse("");

        registry.add("spring.security.oauth2.client.registration.opertusmundi.client-secret", () -> clientSecret);
    }

    /**
     * Token management
     */

    @Value("${spring.security.oauth2.client.registration.opertusmundi.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    private static String getRefreshToken() {
        try (final Keycloak keycloakAdminClient = KeycloakBuilder.builder()
            .serverUrl(keycloakContainer.getAuthServerUrl())
            .realm(KEYCLOAK_ADMIN_REALM)
            .clientId(KEYCLOAK_ADMIN_CLIENT)
            .username(keycloakContainer.getAdminUsername())
            .password(keycloakContainer.getAdminPassword())
            .build()
        ) {
            return keycloakAdminClient.tokenManager().getAccessToken().getRefreshToken();
        }
    }

    protected String getAccessToken() throws URISyntaxException {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", TOPIO_USERNAME);
        map.add("password", "password");
        map.add("client_id", KEYCLOAK_TOPIO_CLIENT);
        map.add("client_secret", clientSecret);

        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        final URI url = new URIBuilder(keycloakContainer.getAuthServerUrl() + "/realms/" + KEYCLOAK_TOPIO_REALM + "/protocol/openid-connect/token")
            .build();

        final ResponseEntity<JsonNode> response = this.restTemplate.postForEntity(
            url,
            request,
            JsonNode.class
        );

        return response.getBody().get("access_token").asText();
    }
}
