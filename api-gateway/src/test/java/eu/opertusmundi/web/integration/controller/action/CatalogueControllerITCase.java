package eu.opertusmundi.web.integration.controller.action;

import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import eu.opertusmundi.common.domain.ProviderTemplateContractHistoryEntity;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.contract.TemplateContractDto;
import eu.opertusmundi.common.model.openapi.schema.CatalogueEndpointTypes;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractHistoryRepository;
import eu.opertusmundi.test.support.integration.AbstractIntegrationTest;
import eu.opertusmundi.test.support.utils.ResponsePayload;

@SpringBootTest
@Sql(
    scripts = {"classpath:sql/truncate-tables.sql"},
    config = @SqlConfig(separator = ScriptUtils.EOF_STATEMENT_SEPARATOR)
)
@Sql(scripts = {
    "classpath:sql/initialize-settings.sql"
})
@Sql(scripts = {
    "classpath:sql/create-helpdesk-account.sql",
    "classpath:sql/create-provider-account.sql",
    "classpath:sql/create-contract-templates.sql"
})
public class CatalogueControllerITCase extends AbstractIntegrationTest {

    final static private UUID notFoundAssetId = UUID.randomUUID();

    /**
     * Configure WireMock server lifecycle events
     */
    @TestConfiguration
    public static class WireMockConfiguration {

        @Value("${opertusmundi.feign.catalogue.url}")
        private String catalogueServiceUrl;

        @Bean(initMethod = "start", destroyMethod = "stop")
        public WireMockServer mockCatalogueServer() throws IOException {
            // Get port from configuration
            final URL url = new URL(this.catalogueServiceUrl);

            final WireMockServer server = new WireMockServer(url.getPort());

            final ResponsePayload searchResponse   = ResponsePayload.from("classpath:responses/catalogue-service/search.json");
            final ResponsePayload itemResponse     = ResponsePayload.from("classpath:responses/catalogue-service/item.json");
            final ResponsePayload notFoundResponse = ResponsePayload.from("classpath:responses/catalogue-service/not-found.json");

            server.stubFor(WireMock.get(urlPathEqualTo("/api/published/search"))
                .atPriority(1)
                .withQueryParam("q", WireMock.equalTo("nothing"))
                .willReturn(WireMock.aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(notFoundResponse.getData())
                ));

            server.stubFor(WireMock.get(urlPathEqualTo("/api/published/search"))
              .atPriority(2)
              .withQueryParam("q", matching("[\\S]*"))
              .willReturn(WireMock.aResponse()
                  .withStatus(HttpStatus.OK.value())
                  .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                  .withBody(searchResponse.getData())
              ));

            server.stubFor(WireMock.get(urlPathEqualTo("/api/published/" + notFoundAssetId.toString()))
                .atPriority(3)
                .willReturn(WireMock.aResponse()
                    .withStatus(HttpStatus.NOT_FOUND.value())
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(notFoundResponse.getData())
                ));

            server.stubFor(WireMock.get(urlPathMatching("/api/published/([a-zA-Z0-9-]+)"))
                .atPriority(4)
                .willReturn(WireMock.aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(itemResponse.getData())
                ));

            return server;
        }
    }

    @Autowired
    private WireMockServer wireMockServer;

    @MockBean
    private ProviderTemplateContractHistoryRepository contractRepository;

    @BeforeEach
    public void setUp() {
        // Object properties must match the values in classpath:/responses/catalogue-service/item.json
        final TemplateContractDto c = new TemplateContractDto();
        c.setId(1);
        c.setKey(UUID.randomUUID());
        c.setTitle("Title");
        c.setVersion("1");

        final ProviderTemplateContractHistoryEntity e = new ProviderTemplateContractHistoryEntity();

        ReflectionTestUtils.invokeSetterMethod(e, "setId", 1);
        ReflectionTestUtils.invokeSetterMethod(e, "setKey", UUID.randomUUID());

        e.setTitle("Template");
        e.setVersion("1");

        Mockito.when(contractRepository.findByIdAndVersion(any(), any(), any())).thenReturn(Optional.of(e));

        Mockito.when(contractRepository.findOneObjectByIdAndVersion(any(), any(), any())).thenReturn(c);
    }

    /**
     * After each test method, reset mock server requests
     */
    @AfterEach
    public void teardown() {
        this.wireMockServer.resetRequests();
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When searching with any valid query, returns data")
    @Disabled(value = "Requires Elasticsearch integration")
    void whenSearchWithValidQuery_returnData() throws Exception {
        final MvcResult mvcResult = this.mockMvc.perform(get("/action/catalogue?page=0&size=10&query=test"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        this.wireMockServer.verify(1, WireMock.getRequestedFor(urlPathEqualTo("/api/published/search"))
            .withQueryParam("q", WireMock.equalTo("test")));

        final String                                        content  = mvcResult.getResponse().getContentAsString();
        final RestResponse<PageResultDto<CatalogueItemDto>> response =
            this.objectMapper.readValue(content, CatalogueEndpointTypes.ItemCollectionResponse.class);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessages()).isEmpty();
        assertThat(response.getResult()).isNotNull();

        final PageResultDto<CatalogueItemDto> result = response.getResult();

        assertThat(result.getCount()).isEqualTo(2);
        assertThat(result.getPageRequest()).isNotNull();
        assertThat(result.getPageRequest().getPage()).isEqualTo(0);
        assertThat(result.getPageRequest().getSize()).isEqualTo(10);
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When query selects no items, returns empty result")
    @Disabled(value = "Requires Elasticsearch integration")
    void whenQueryThatSelectsNoItems_returnError() throws Exception {
        final MvcResult mvcResult = this.mockMvc.perform(get("/action/catalogue?page=0&size=10&query=nothing"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        this.wireMockServer.verify(1, WireMock.getRequestedFor(urlPathEqualTo("/api/published/search"))
            .withQueryParam("q", WireMock.equalTo("nothing")));

        final String                                        content  = mvcResult.getResponse().getContentAsString();
        final RestResponse<PageResultDto<CatalogueItemDto>> response =
            this.objectMapper.readValue(content, CatalogueEndpointTypes.ItemCollectionResponse.class);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessages()).isEmpty();
        assertThat(response.getResult()).isNotNull();

        final PageResultDto<CatalogueItemDto> result = response.getResult();

        assertThat(result.getCount()).isEqualTo(0);
        assertThat(result.getItems()).isNotNull();
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getPageRequest()).isNotNull();
        assertThat(result.getPageRequest().getPage()).isEqualTo(0);
        assertThat(result.getPageRequest().getSize()).isEqualTo(10);
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When searching for existing item, returns data")
    void whenSearchForItem_returnData() throws Exception {
        final MvcResult mvcResult = this.mockMvc.perform(get("/action/catalogue/items/{id}", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        this.wireMockServer.verify(1, WireMock.getRequestedFor(urlPathMatching("/api/published/([a-zA-Z0-9-]+)")));

        final String                                content  = mvcResult.getResponse().getContentAsString();
        final RestResponse<CatalogueItemDetailsDto> response = this.objectMapper.readValue(content, CatalogueEndpointTypes.ItemResponse.class);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessages()).isEmpty();
        assertThat(response.getResult()).isNotNull();
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When searching for item that does not exist, returns error")
    void whenSearchForItemThatDoesNotExist_returnError() throws Exception {
        this.mockMvc.perform(get("/action/catalogue/items/{id}", CatalogueControllerITCase.notFoundAssetId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(BasicMessageCode.NotFound.key()))
            .andExpect(jsonPath("$.result").doesNotExist());

        this.wireMockServer.verify(1, WireMock.getRequestedFor(urlPathEqualTo("/api/published/" + notFoundAssetId.toString())));
    }

}
