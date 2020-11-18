package eu.opertusmundi.web.config;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
public class FileSystemConfiguration {

    private Path tempDir;

    private Path userDataDir;

    @Autowired
    private void setTempDataDir(@Value("${opertusmundi.file-system.temp-dir}") String d) {
        final Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.tempDir = path;
    }

    @Autowired
    private void setUserDataDir(@Value("${opertusmundi.file-system.data-dir}") String d) {
        final Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.userDataDir = path;
    }

    @PostConstruct
    private void initialize() throws IOException {
        for (final Path dataDir : Arrays.asList(this.tempDir, this.userDataDir)) {
            try {
                Files.createDirectories(dataDir);
            } catch (final FileAlreadyExistsException ex) {

            }
        }
    }

    @Bean
    Path tempDataDirectory() {
        return this.tempDir;
    }

    @Bean
    Path userDataDirectory() {
        return this.userDataDir;
    }

}
