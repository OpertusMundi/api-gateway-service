package eu.opertusmundi.test.support.integration;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("testing")
@TestInstance(Lifecycle.PER_CLASS)
@Testcontainers
public class BaseIntegrationTest {

    private static final String POSTGIS_IMAGE_TAG = "10-2.5-alpine";
    private static final String DB_NAME           = "opertus-mundi";
    private static final String DB_USERNAME       = "dev";
    private static final String DB_PASSWORD       = "dev";

    protected static final JdbcDatabaseContainer<?> postgisContainer = new PostgisContainerProvider()
        .newInstance(POSTGIS_IMAGE_TAG)
        .withDatabaseName(DB_NAME)
        .withUsername(DB_USERNAME)
        .withPassword(DB_PASSWORD);

    static {
        postgisContainer.start();
    }

    @DynamicPropertySource
    private static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgisContainer::getJdbcUrl);
        registry.add("spring.datasource.username", () -> DB_USERNAME);
        registry.add("spring.datasource.password", () -> DB_PASSWORD);

        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.baseline-version", () -> "0_0_0");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
        registry.add("spring.flyway.locations", () -> "filesystem:${application.parent.project.base-dir}/cli/src/main/resources/db/migration");
        registry.add("spring.flyway.schemas", () -> "public,web");
        registry.add("spring.flyway.table", () -> "db_version");
    }
}
