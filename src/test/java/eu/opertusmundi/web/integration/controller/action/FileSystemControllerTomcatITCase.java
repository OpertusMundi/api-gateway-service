package eu.opertusmundi.web.integration.controller.action;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountCommandDto;
import eu.opertusmundi.common.model.file.FileUploadCommand;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.web.utils.AccountCommandFactory;

/**
 * Integration test for testing file upload request max size. Since the file
 * size is controlled by the integrated Tomcat server, a full server is spawned.
 *
 * The request is made using a {@link RestTemplate} instance. Using an instance
 * of {@link TestRestTemplate} caused a java.net.SocketException with error
 * Connection reset; Hence the response could not be parsed for making
 * assertions.
 *
 * By default, the max file size is set to 1MB in the
 * application-testing.properties configuration file and can be set using the
 * spring.servlet.multipart.max-file-size property.
 *
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
    // By default bean definitions cannot be overridden.
    properties = {"spring.main.allow-bean-definition-overriding=true"}
)
@ActiveProfiles("testing")
@TestInstance(Lifecycle.PER_CLASS)
public class FileSystemControllerTomcatITCase {

    @TestConfiguration
    public static class FileSystemConfiguration {

        public final static String relativePath = UUID.randomUUID().toString();

        private Path createDirectory(String suffix) throws IOException {
            final Path path = Paths.get(FileUtils.getTempDirectory().getAbsolutePath(), relativePath, suffix);
            FileUtils.forceMkdir(path.toFile());
            return path;
        }

        @Bean
        Path userDataDirectory() throws IOException {
            return this.createDirectory("user");
        }
    }

    @LocalServerPort
    private int port;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Value("classpath:assets/file-2.zip")
    private File largeFile;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AccountRepository accountRepository;

    private RestTemplate restTemplate;

    @BeforeAll
    public void setupAccounts() {
        // Reset database
        JdbcTestUtils.deleteFromTables(this.jdbcTemplate, "web.account");

        // Create default account with authority ROLE_USER
        final AccountCommandDto command = AccountCommandFactory.user().build();

        this.accountRepository.create(command);
    }

    @BeforeEach
    public void setup() {
        this.restTemplate = new RestTemplate();
    }

    @AfterAll
    public static void teardown () {
        final Path path = Paths.get(
            FileUtils.getTempDirectory().getAbsolutePath(),
            FileSystemControllerTomcatITCase.FileSystemConfiguration.relativePath
        );

        FileUtils.deleteQuietly(path.toFile());
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When uploading large file for authenticated user, return 413")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenAuthenticatedUserUploadLargeFile_return413() throws Exception {
        // Upload a file large enough to raise a MaxUploadSizeExceededException
        // exception
        assertThat(DataSize.parse(this.maxFileSize, DataUnit.BYTES).toBytes()).isLessThan(this.largeFile.length());

        final FileUploadCommand command = new FileUploadCommand();
        command.setComment("Test");
        command.setFilename("file-2.zip");
        command.setOverwrite(false);
        command.setPath("/test");

        final MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        bodyBuilder.part("file", FileUtils.readFileToByteArray(this.largeFile))
            .header("Content-Disposition", "form-data; name=file; filename=file-2.zip")
            .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);

        bodyBuilder.part("data", this.objectMapper.writeValueAsString(command))
            .header("Content-Disposition", "form-data; name=data")
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        final HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity = new HttpEntity<>(bodyBuilder.build(), headers);

        ResponseEntity<String>   response = null;
        HttpClientErrorException ex       = null;

        try {
            response = this.restTemplate.exchange(
                String.format("http://localhost:%d/action/file-system/files", this.port),
                HttpMethod.POST,
                requestEntity,
                String.class
            );
        } catch (final HttpClientErrorException restEx) {
            ex = restEx;
        }

        assertThat(response).isNull();
        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);

        final RestResponse<Void> data = this.objectMapper.readValue(ex.getResponseBodyAsString(), new TypeReference<RestResponse<Void>>() {
        });

        assertThat(data.getMessages()).hasSize(1);
        assertThat(data.getMessages().get(0).getCode()).isEqualTo(BasicMessageCode.PayloadTooLarge.key());
    }

}
