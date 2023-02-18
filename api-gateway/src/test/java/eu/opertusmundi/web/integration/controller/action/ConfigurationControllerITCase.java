package eu.opertusmundi.web.integration.controller.action;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.test.support.integration.AbstractIntegrationTest;

@SpringBootTest
@Sql(
    scripts = {"classpath:sql/truncate-tables.sql"},
    config = @SqlConfig(separator = ScriptUtils.EOF_STATEMENT_SEPARATOR)
)
@Sql(scripts = {
    "classpath:sql/initialize-settings.sql"
})
public class ConfigurationControllerITCase extends AbstractIntegrationTest {

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When valid URL, return configuration data")
    void whenValidUrl_returnConfigurationData() throws Exception {
        this.mockMvc.perform(get("/action/configuration/{locale}", "el")
            .contentType("application/json"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").isNotEmpty())
            .andExpect(jsonPath("$.result.authProviders").isArray())
            .andExpect(jsonPath("$.result.authProviders", hasSize(1)))
            .andExpect(jsonPath("$.result.authProviders[?(@ == '" + EnumAuthProvider.Forms.toString() + "')]").exists());
    }

}
