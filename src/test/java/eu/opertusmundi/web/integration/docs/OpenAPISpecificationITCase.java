package eu.opertusmundi.web.integration.docs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@ActiveProfiles("testing")
@AutoConfigureMockMvc
public class OpenAPISpecificationITCase {

    @Value("${application.project.base-dir}")
    private String baseDir;

    @Value("${springdoc.api-docs.path}")
    private String endpoint;

    @Value("${opertus-mundi.open-api.spec:openapi.json}")
    private String openApiSpecFilename;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Tag(value = "Documentation")
    @DisplayName(value = "When valid URL, method and content-type, return 200")
    void whenValidUrlAndMethodAndContentType_thenReturns200() throws Exception {
        final MvcResult result = this.mockMvc.perform(get(this.endpoint)
            .contentType("application/json"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        final String content = result.getResponse().getContentAsString();
        final Path   path    = Paths.get(this.baseDir, "docs", this.openApiSpecFilename);
        final File   file    = path.toFile();

        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
    }
}
