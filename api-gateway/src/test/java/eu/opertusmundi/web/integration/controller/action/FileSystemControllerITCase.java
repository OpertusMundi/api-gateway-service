package eu.opertusmundi.web.integration.controller.action;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.common.net.HttpHeaders;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.Message.EnumLevel;
import eu.opertusmundi.common.model.file.FilePathCommand;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;
import eu.opertusmundi.common.model.file.FileUploadCommand;
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
        Path tempDirectory() throws IOException {
            return this.createDirectory("tmp");
        }

        @Bean
        Path userDirectory() throws IOException {
            final Path path = this.createDirectory("user");

            // Create test user home directory (this directory is created on
            // user registration)
            this.createDirectory("user/user@opertusmundi.eu");

            return path;
        }

        @Bean
        Path draftDirectory() throws IOException {
            return this.createDirectory("drafts");
        }

        @Bean
        Path assetDirectory() throws IOException {
            return this.createDirectory("assets");
        }
    }

    @Value("classpath:assets/file-1.zip")
    private File uploadedFile;

    @AfterAll
    public void teardown () {
        final Path path = Paths.get(
            FileUtils.getTempDirectory().getAbsolutePath(),
            FileSystemControllerITCase.FileSystemConfiguration.relativePath
        );

        FileUtils.deleteQuietly(path.toFile());
    }

    @Test
    @Order(10)
    @Tag(value = "Controller")
    @DisplayName(value = "When browsing file system for anonymous user, return 403")
    void whenAnonymousBrowse_return403() throws Exception {
        this.mockMvc.perform(get("/action/file-system")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(BasicMessageCode.Forbidden.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.messages[0].description").value(
                this.messageSource.getMessage(BasicMessageCode.Forbidden.key(), null, Locale.getDefault()))
            );
    }

    @Test
    @Order(11)
    @Tag(value = "Controller")
    @DisplayName(value = "When browsing file system for authenticated user, return file system")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserBrowse_returnFileSystem() throws Exception {
        this.mockMvc.perform(get("/action/file-system")
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
    @Order(20)
    @Tag(value = "Controller")
    @DisplayName(value = "When creating folder for authenticated user, return file system")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserCreateFolder_returnFileSystem() throws Exception {
        final FilePathCommand command = FilePathCommand.builder()
            .path("/test")
            .build();

        this.mockMvc.perform(post("/action/file-system/folders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages").isEmpty())
            .andExpect(jsonPath("$.result").exists())
            .andExpect(jsonPath("$.result.count").value(1))
            .andExpect(jsonPath("$.result.size").value(0))
            .andExpect(jsonPath("$.result.path").value("/"))
            .andExpect(jsonPath("$.result.name").value("/"))
            .andExpect(jsonPath("$.result.files").isArray())
            .andExpect(jsonPath("$.result.files").isEmpty())
            .andExpect(jsonPath("$.result.folders").isArray())
            .andExpect(jsonPath("$.result.folders", hasSize(1)))
            .andExpect(jsonPath("$.result.folders[0].path").value("/test"))
            .andExpect(jsonPath("$.result.folders[0].name").value("test"))
            .andExpect(jsonPath("$.result.modified").isNotEmpty());
    }

    @Test
    @Order(21)
    @Tag(value = "Controller")
    @DisplayName(value = "When creating existing folder for authenticated user, return error")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserCreateExistingFolder_returnError() throws Exception {
        final FilePathCommand command = FilePathCommand.builder()
            .path("/test")
            .build();

        this.mockMvc.perform(post("/action/file-system/folders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(FileSystemMessageCode.PATH_ALREADY_EXISTS.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.exception").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())
            .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @Order(22)
    @Tag(value = "Controller")
    @DisplayName(value = "When creating folder for authenticated user that exceeds nesting max depth, return error")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserCreateFolderMaxDepth_returnError() throws Exception {
        final FilePathCommand command = FilePathCommand.builder()
            .path("/1/2/3/4/5/6/7/8/9/10")
            .build();

        this.mockMvc.perform(post("/action/file-system/folders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(FileSystemMessageCode.PATH_MAX_DEPTH.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.exception").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())
            .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @Order(23)
    @Tag(value = "Controller")
    @DisplayName(value = "When creating folder for authenticated user with invalid path name, return error")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserCreateFolderWithInvalidName_returnError() throws Exception {
        final FilePathCommand command = FilePathCommand.builder()
            .path("/test$1/")
            .build();

        this.mockMvc.perform(post("/action/file-system/folders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(FileSystemMessageCode.INVALID_PATH.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.exception").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())
            .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @Order(24)
    @Tag(value = "Controller")
    @DisplayName(value = "When creating folder for authenticated user with invalid path length, return error")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserCreateFolderWithInvalidPathLength_returnError() throws Exception {
        final FilePathCommand command = FilePathCommand.builder()
            .path("/" + StringUtils.repeat("test", 100))
            .build();

        this.mockMvc.perform(post("/action/file-system/folders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(FileSystemMessageCode.PATH_MAX_LENGTH.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.exception").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())
            .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @Order(30)
    @Tag(value = "Controller")
    @DisplayName(value = "When uploading file for authenticated user, return file system")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserUploadFile_returnFileSystem() throws Exception {
        this.mockMvc.perform(this.createUploadRequest(false, this.uploadedFile))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages").isEmpty())
            .andExpect(jsonPath("$.result").exists())
            .andExpect(jsonPath("$.result.count").value(1))
            .andExpect(jsonPath("$.result.size").value(this.uploadedFile.length()))
            .andExpect(jsonPath("$.result.path").value("/"))
            .andExpect(jsonPath("$.result.name").value("/"))
            .andExpect(jsonPath("$.result.files").isArray())
            .andExpect(jsonPath("$.result.files").isEmpty())
            .andExpect(jsonPath("$.result.folders").isArray())
            .andExpect(jsonPath("$.result.folders", hasSize(1)))
            .andExpect(jsonPath("$.result.folders[0].path").value("/test"))
            .andExpect(jsonPath("$.result.folders[0].name").value("test"))
            .andExpect(jsonPath("$.result.folders[0].files").isArray())
            .andExpect(jsonPath("$.result.folders[0].files", hasSize(1)))
            .andExpect(jsonPath("$.result.folders[0].files[0].size").value(this.uploadedFile.length()))
            .andExpect(jsonPath("$.result.folders[0].files[0].path").value("/test/1.zip"))
            .andExpect(jsonPath("$.result.folders[0].files[0].name").value("1.zip"))
            .andExpect(jsonPath("$.result.modified").isNotEmpty());
    }

    @Test
    @Order(31)
    @Tag(value = "Controller")
    @DisplayName(value = "When uploading existing file for authenticated user, return error")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserUploadExistingFile_returnError() throws Exception {
        this.mockMvc.perform(this.createUploadRequest(false, this.uploadedFile))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(FileSystemMessageCode.PATH_ALREADY_EXISTS.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.exception").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())
            .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @Order(32)
    @Tag(value = "Controller")
    @DisplayName(value = "When overwriting file for authenticated user, return file system")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserUploadOverwriteFile_returnFileSystem() throws Exception {
        this.mockMvc.perform(this.createUploadRequest(true, this.uploadedFile))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages").isEmpty())
            .andExpect(jsonPath("$.result").isNotEmpty());
    }

    @Test
    @Order(40)
    @Tag(value = "Controller")
    @DisplayName(value = "When downloading file for authenticated user, return file")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserDownloadFile_returnFile() throws Exception {
        this.mockMvc.perform(get("/action/file-system/files")
            .queryParam("path","/test/1.zip"))
            .andExpect(content().contentType("application/zip"))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=1.zip"))
            .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, this.uploadedFile.length()))
            .andExpect(status().isOk());
    }

    @Test
    @Order(41)
    @Tag(value = "Controller")
    @DisplayName(value = "When downloading missing file for authenticated user, return 500")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserDownloadMissingFile_return500() throws Exception {
        this.mockMvc.perform(get("/action/file-system/files")
            .queryParam("path","/test/unknown.zip"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(FileSystemMessageCode.PATH_NOT_FOUND.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.exception").doesNotExist());
    }

    @Test
    @Order(50)
    @Tag(value = "Controller")
    @DisplayName(value = "When deleting file for authenticated user, return file system")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserDeleteFile_returnFileSystem() throws Exception {
        this.mockMvc.perform(delete("/action/file-system")
            .queryParam("path","/test/1.zip"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages").isEmpty())
            .andExpect(jsonPath("$.result").exists())
            .andExpect(jsonPath("$.result.count").value(1))
            .andExpect(jsonPath("$.result.size").value(0))
            .andExpect(jsonPath("$.result.path").value("/"))
            .andExpect(jsonPath("$.result.name").value("/"))
            .andExpect(jsonPath("$.result.files").isArray())
            .andExpect(jsonPath("$.result.files").isEmpty())
            .andExpect(jsonPath("$.result.folders").isArray())
            .andExpect(jsonPath("$.result.folders", hasSize(1)))
            .andExpect(jsonPath("$.result.folders[0].path").value("/test"))
            .andExpect(jsonPath("$.result.folders[0].name").value("test"))
            .andExpect(jsonPath("$.result.modified").isNotEmpty());
    }

    @Test
    @Order(51)
    @Tag(value = "Controller")
    @DisplayName(value = "When deleting missing file for authenticated user, return error")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserDeleteMissingFile_returnError() throws Exception {
        this.mockMvc.perform(delete("/action/file-system")
            .queryParam("path","/test/1.zip"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(FileSystemMessageCode.PATH_NOT_FOUND.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.exception").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())
            .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @Order(60)
    @Tag(value = "Controller")
    @DisplayName(value = "When file system is missing, return error")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserBrowseMissingFileSystem_returnError() throws Exception {
        // Perform tear down operation manually
        final Path path = Paths.get(
            FileUtils.getTempDirectory().getAbsolutePath(),
            FileSystemControllerITCase.FileSystemConfiguration.relativePath
        );

        FileUtils.deleteQuietly(path.toFile());

        this.mockMvc.perform(get("/action/file-system"))
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

    private MockMultipartHttpServletRequestBuilder createUploadRequest(boolean overwrite, File resource) throws IOException {
        final FileUploadCommand command = new FileUploadCommand();
        command.setComment("Test");
        command.setFileName("1.zip");
        command.setOverwrite(overwrite);
        command.setPath("/test");

        final MockMultipartFile file = new MockMultipartFile(
            "file",
            "1.zip",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            FileUtils.readFileToByteArray(resource)
        );

        final MockMultipartFile data = new MockMultipartFile(
            "data",
            null,
            MediaType.APPLICATION_JSON_VALUE,
            this.objectMapper.writeValueAsString(command).getBytes()
        );

        return MockMvcRequestBuilders.multipart("/action/file-system/files")
            .file(file)
            .file(data);
    }

}
