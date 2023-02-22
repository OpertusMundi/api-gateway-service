package eu.opertusmundi.web.integration.controller.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.EnumAccountType;
import eu.opertusmundi.common.model.Message.EnumLevel;
import eu.opertusmundi.common.model.account.EnumAccountAttribute;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;
import eu.opertusmundi.common.model.keycloak.server.UserDto;
import eu.opertusmundi.common.model.keycloak.server.UserQueryDto;
import eu.opertusmundi.common.service.KeycloakAdminService;
import eu.opertusmundi.test.support.integration.AbstractIntegrationTestWithSecurity;

@SpringBootTest(
    // By default bean definitions cannot be overridden.
    properties = {"spring.main.allow-bean-definition-overriding=true"}
)
@Sql(
    scripts = {"classpath:sql/truncate-tables.sql"},
    config = @SqlConfig(separator = ScriptUtils.EOF_STATEMENT_SEPARATOR)
)
@Sql(scripts = {
    "classpath:sql/initialize-settings.sql",
    "classpath:sql/create-marketplace-account.sql"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileSystemControllerITCase extends AbstractIntegrationTestWithSecurity {

    @TestConfiguration
    public static class FileSystemConfiguration {

        private static final Set<PosixFilePermission> DEFAULT_DIRECTORY_PERMISSIONS = PosixFilePermissions.fromString("rwxrwxr-x");

        public final static String relativePath = UUID.randomUUID().toString();

        private Path createDirectory(String suffix) throws IOException {
            final Path path = Paths.get(FileUtils.getTempDirectory().getAbsolutePath(), relativePath, suffix);

            Files.createDirectories(path);
            Files.setPosixFilePermissions(path, DEFAULT_DIRECTORY_PERMISSIONS);

            return path;
        }

        @Bean
        Path userDirectory() throws IOException {
            final Path path = this.createDirectory("user");
            this.createDirectory("user/user@opertusmundi.eu");
            return path;
        }
    }

    @Value("classpath:assets/file-1.zip")
    private File sourceFile;

    @Autowired
    private Path userDirectory;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    @BeforeAll
    public void setup() throws URISyntaxException {
        final UserQueryDto queryForUsername = new UserQueryDto();
        queryForUsername.setUsername(TOPIO_USERNAME);
        queryForUsername.setExact(true);

        final List<UserDto> usersForUsername = keycloakAdminService.findUsers(queryForUsername);
        Assert.state(usersForUsername.size() < 2,
            () -> "expected no more than one IDP user for a given username [username=" + TOPIO_USERNAME + "]");

        UserDto user = null;
        if (usersForUsername.isEmpty()) {
            // Create the user
            user = new UserDto();
            user.setUsername(TOPIO_USERNAME);
            user.setEmail(TOPIO_USERNAME);
            user.setEmailVerified(true);
            user.setEnabled(true);
            // Add opertusmundi-specific attributes (accountType etc.)
            user.setAttributes(Collections.singletonMap(
                EnumAccountAttribute.ACCOUNT_TYPE.key(), new String[]{EnumAccountType.OPERTUSMUNDI.toString()}
            ));
            final UUID userId = keycloakAdminService.createUser(user);
            user = keycloakAdminService.getUser(userId).get();
        } else {
            // The user is present; just retrieve first of singleton result
            user = usersForUsername.get(0);
        }

        Assert.state(user.getId() != null, "expected a non-null user identifier (from the IDP side)");
        keycloakAdminService.resetPasswordForUser(user.getId(), "password", false /* temporary */);
    }

    @AfterAll
    public void teardown () {
        final Path path = Paths.get(
            FileUtils.getTempDirectory().getAbsolutePath(),
            FileSystemControllerITCase.FileSystemConfiguration.relativePath
        );

        FileUtils.deleteQuietly(path.toFile());
    }

    @Test
    @Order(1)
    @Tag(value = "Controller")
    @DisplayName(value = "When browsing file system for unauthenticated user, return 401")
    void whenAnonymousBrowse_return401() throws Exception {
        this.mockMvc.perform(get("/api/file-system"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    @Tag(value = "Controller")
    @DisplayName(value = "When downloading file for unauthenticated user, return 401")
    void whenAnonymousDownload_return401() throws Exception {
        this.mockMvc.perform(get("/api/file-system/files")
            .queryParam("path","/1.zip"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @Tag(value = "Controller")
    @DisplayName(value = "When browsing file system for authenticated user, return file system")
    void whenAuthenticatedUserBrowse_returnFileSystem() throws Exception {
        final var accessToken = this.getAccessToken();

        this.mockMvc.perform(get("/api/file-system")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages").isEmpty())
            .andExpect(jsonPath("$.result").exists())
            .andExpect(jsonPath("$.result.count").value(0))
            .andExpect(jsonPath("$.result.size").value(0))
            .andExpect(jsonPath("$.result.path").value("/"))
            .andExpect(jsonPath("$.result.name").value("/"))
            .andExpect(jsonPath("$.result.files").isArray())
            .andExpect(jsonPath("$.result.files").isEmpty())
            .andExpect(jsonPath("$.result.folders").isArray())
            .andExpect(jsonPath("$.result.folders").isEmpty())
            .andExpect(jsonPath("$.result.modified").isNotEmpty());
    }

    @Test
    @Order(4)
    @Tag(value = "Controller")
    @DisplayName(value = "When downloading file for authenticated user, return file")
    void whenAuthenticatedUserDownloadFile_returnFile() throws Exception {
        final var accessToken = this.getAccessToken();
        final var targetFile  = userDirectory.resolve("user@opertusmundi.eu/1.zip").toFile();
        FileUtils.copyFile(sourceFile, targetFile);

        this.mockMvc.perform(get("/api/file-system/files")
            .header("Authorization", "Bearer " + accessToken)
            .queryParam("path","/1.zip"))
            .andExpect(content().contentType("application/zip"))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=1.zip"))
            .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, sourceFile.length()))
            .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    @Tag(value = "Controller")
    @DisplayName(value = "When downloading missing file for authenticated user, return 404")
    void whenAuthenticatedUserDownloadMissingFile_return404() throws Exception {
        final var accessToken = this.getAccessToken();

        this.mockMvc.perform(get("/api/file-system/files")
            .header("Authorization", "Bearer " + accessToken)
            .queryParam("path","/unknown.zip"))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    @Tag(value = "Controller")
    @DisplayName(value = "When file system is missing, return error")
    void whenAuthenticatedUserBrowseMissingFileSystem_returnError() throws Exception {
        final var accessToken = this.getAccessToken();

        // Perform tear down operation manually
        final Path path = Paths.get(
            FileUtils.getTempDirectory().getAbsolutePath(),
            FileSystemControllerITCase.FileSystemConfiguration.relativePath
        );

        FileUtils.deleteQuietly(path.toFile());

        this.mockMvc.perform(get("/api/file-system")
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(FileSystemMessageCode.IO_ERROR.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.exception").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())
            .andExpect(jsonPath("$.result").doesNotExist());
    }

}
