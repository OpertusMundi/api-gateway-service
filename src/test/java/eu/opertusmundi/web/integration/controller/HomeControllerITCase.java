package eu.opertusmundi.web.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.Message.EnumLevel;
import eu.opertusmundi.web.integration.support.AbstractIntegrationTest;

@SpringBootTest
class HomeControllerITCase extends AbstractIntegrationTest {

    @Value("${springdoc.api-docs.server:http://localhost:8080}")
    private String serverUrl;

    @Value("${springdoc.api-docs.path}")
    private String openApiSpec;

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When default URL, return index view")
    void whenDefaultUrl_returnIndexView() throws Exception {
        final MvcResult result = this.mockMvc.perform(get("/")
            .contentType(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getModelAndView().getViewName()).isEqualTo("index");
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When documentation URL, return docs view")
    void whenDocumentationUrl_returnDocsView() throws Exception {
        final MvcResult result = this.mockMvc.perform(get("/docs")
            .contentType(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(this.serverUrl).isNotBlank();
        assertThat(this.openApiSpec).isNotBlank();

        assertThat(result.getModelAndView().getViewName()).isEqualTo("docs");
        assertThat(result.getModelAndView().getModel().get("endpoint")).isEqualTo(this.serverUrl + this.openApiSpec);
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When verify token URL, return index view")
    void whenVerifyTokenUrl_returnIndexView() throws Exception {
        final MvcResult result = this.mockMvc.perform(get("/token/verify")
            .queryParam("token", UUID.randomUUID().toString()))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getModelAndView().getViewName()).isEqualTo("index");
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When HTML error page URL, return index view")
    void whenHtmlErrorPageUrl_returnIndexView() throws Exception {
        final MvcResult result = this.mockMvc.perform(get("/error/{id}", HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getModelAndView().getViewName()).isEqualTo("index");
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When JSON error URL, return RestResponse entity")
    void whenJsonErrorUrl_returnsRestResponseEntity() throws Exception {
        this.mockMvc.perform(get("/error/{id}", HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(BasicMessageCode.InternalServerError.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.result").doesNotExist());
    }
}
