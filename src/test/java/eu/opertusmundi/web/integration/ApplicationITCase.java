package eu.opertusmundi.web.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import eu.opertusmundi.web.controller.HomeController;

@SpringBootTest
@ActiveProfiles("testing")
@AutoConfigureMockMvc
public class ApplicationITCase {

    @Autowired
    private HomeController controller;

    @Test
    @DisplayName(value = "Context Loads")
    public void contextLoads() throws Exception {
        assertThat(this.controller).isNotNull();
    }

}
