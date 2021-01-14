package eu.opertusmundi.web.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import eu.opertusmundi.web.controller.HomeController;
import eu.opertusmundi.web.integration.support.AbstractIntegrationTest;

@SpringBootTest
public class ApplicationITCase extends AbstractIntegrationTest {

    @Autowired
    private HomeController controller;

    @Test
    @DisplayName(value = "Context Loads")
    public void contextLoads() throws Exception {
        assertThat(this.controller).isNotNull();
    }

}
