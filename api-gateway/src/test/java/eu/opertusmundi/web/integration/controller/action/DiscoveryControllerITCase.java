package eu.opertusmundi.web.integration.controller.action;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import eu.opertusmundi.common.model.openapi.schema.DiscoveryEndpointTypes;
import eu.opertusmundi.web.integration.support.AbstractIntegrationTest;
import eu.opertusmundi.web.utils.ResponsePayload;

@SpringBootTest
public class DiscoveryControllerITCase extends AbstractIntegrationTest {

    /**
     * Configure WireMock server lifecycle events
     */
    @TestConfiguration
    public static class WireMockConfiguration {

        @Value("${opertusmundi.feign.discovery.url}")
        private String discoverServiceUrl;

        @Bean(initMethod = "start", destroyMethod = "stop")
        public WireMockServer mockDiscoveryServer() throws IOException {
            // Get port from configuration
            final URL url = new URL(this.discoverServiceUrl);

            final WireMockServer server = new WireMockServer(url.getPort());

            final ResponsePayload joinableResponse = ResponsePayload.from("classpath:responses/discovery-service/joinable.json");
            final ResponsePayload relatedResponse   = ResponsePayload.from("classpath:responses/discovery-service/related.json");

            server.stubFor(WireMock.get(urlPathEqualTo("/get-joinable"))
                .withQueryParam("asset_id", WireMock.equalTo("1"))
                .willReturn(WireMock.aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(joinableResponse.getData())
                ));

            server.stubFor(WireMock.get(urlPathEqualTo("/get-related"))
              .withQueryParam("source_asset_id",  WireMock.equalTo("1"))
              .withQueryParam("target_asset_ids", WireMock.equalTo("2,3"))
              .willReturn(WireMock.aResponse()
                  .withStatus(HttpStatus.OK.value())
                  .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                  .withBody(relatedResponse.getData())
              ));

            return server;
        }
    }

    @Autowired
    private WireMockServer wireMockServer;

    /**
     * After each test method, reset mock server requests
     */
    @AfterEach
    public void teardown() {
        this.wireMockServer.resetRequests();
    }

    @Test
    @Tag(value = "Discovery Controller")
    @DisplayName(value = "When searching joinable, returns data")
    void whenSearchJoinable_returnData() throws Exception {
        final MvcResult mvcResult = this.mockMvc.perform(get("/action/discovery/joinable/1"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        this.wireMockServer.verify(1, WireMock.getRequestedFor(urlPathEqualTo("/get-joinable"))
            .withQueryParam("asset_id", WireMock.equalTo("1")));

        final var content  = mvcResult.getResponse().getContentAsString();
        final var response = this.objectMapper.readValue(content, DiscoveryEndpointTypes.JoinableResponse.class);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessages()).isEmpty();
        assertThat(response.getResult()).isNotNull();

        final var result = response.getResult();

        final var joinableTables = result.getJoinableTables();
        assertThat(joinableTables).isNotNull();
        assertThat(joinableTables).isNotEmpty();
        assertThat(joinableTables).hasSize(1);

        final var table = joinableTables.get(0);
        assertThat(table.getTableName()).isEqualTo("value 4");

        final var matches = table.getMatches();
        assertThat(matches).isNotNull();
        assertThat(matches).isNotEmpty();
        assertThat(matches).hasSize(1);

        final var match = table.getMatches().get(0);
        assertThat(match.getExplanation()).isEqualTo("value 3");
    }

    @Test
    @Tag(value = "Discovery Controller")
    @DisplayName(value = "When searching related, returns data")
    void whenSearchRelated_returnData() throws Exception {
        final MvcResult mvcResult = this.mockMvc.perform(get("/action/discovery/related/1?target=2&target=3"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        this.wireMockServer.verify(1, WireMock.getRequestedFor(urlPathEqualTo("/get-related"))
            .withQueryParam("source_asset_id", WireMock.equalTo("1"))
            .withQueryParam("target_asset_ids", WireMock.equalTo("2,3")));

        final var content  = mvcResult.getResponse().getContentAsString();
        final var response = this.objectMapper.readValue(content, DiscoveryEndpointTypes.RelatedResponse.class);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessages()).isEmpty();
        assertThat(response.getResult()).isNotNull();

        final var result = response.getResult();

        final var joinableTables = result.getRelatedTables();
        assertThat(joinableTables).isNotNull();
        assertThat(joinableTables).isNotEmpty();
        assertThat(joinableTables).hasSize(1);

        final var table = joinableTables.get(0);
        assertThat(table.getExplanation()).isEqualTo("value 3");
        assertThat(table.getLinks()).contains("value 1", "value 2");
    }

}
