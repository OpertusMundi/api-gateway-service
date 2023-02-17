package eu.opertusmundi.web.integration.service.ogc;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.stream.Stream;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.service.ogc.WmsClient;
import eu.opertusmundi.test.support.integration.AbstractIntegrationTest;
import eu.opertusmundi.test.support.utils.BinaryResponsePayload;
import eu.opertusmundi.test.support.utils.ResponsePayload;

@SpringBootTest
@Order(101)
public class WmsClientITCase extends AbstractIntegrationTest {

    private final static String PATH_TEMPLATE = "/wms/%d";

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Configure WireMock server lifecycle events
     */
    @TestConfiguration
    public static class WireMockConfiguration {

        @Value("${opertusmundi.geoserver.endpoint}")
        private String wmsServerUrl;

        @Bean(initMethod = "start", destroyMethod = "stop")
        public WireMockServer mockWmsServer() throws IOException {
            // Get port from configuration
            final URL url = new URL(this.wmsServerUrl);

            final WireMockServer server = new WireMockServer(url.getPort());

            final ResponsePayload[] getCapabilities =  {
                ResponsePayload.from("classpath:responses/wms-service/1.xml"),
                ResponsePayload.from("classpath:responses/wms-service/2.xml"),
                ResponsePayload.from("classpath:responses/wms-service/3.xml"),
            };

            final BinaryResponsePayload getLegendGraphic = BinaryResponsePayload.from("classpath:assets/wms-legend-image.png");

            int index = 1;

            for (final ResponsePayload r : getCapabilities) {
                server.stubFor(WireMock.get(urlPathEqualTo(String.format(PATH_TEMPLATE, index)))
                    .withQueryParam("SERVICE", WireMock.equalToIgnoreCase("WMS"))
                    .withQueryParam("VERSION", WireMock.equalToIgnoreCase("1.3.0"))
                    .withQueryParam("REQUEST", WireMock.equalToIgnoreCase("GetCapabilities"))
                    .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_XML_VALUE)
                        .withBody(r.getData())
                    ));

                server.stubFor(WireMock.get(urlPathEqualTo(String.format(PATH_TEMPLATE, index)))
                        .withQueryParam("service", WireMock.equalToIgnoreCase("WMS"))
                        .withQueryParam("request", WireMock.equalToIgnoreCase("GetLegendGraphic"))
                        .withQueryParam("format", WireMock.equalToIgnoreCase("image/png"))
                        .willReturn(WireMock.aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader("Content-Type", MediaType.IMAGE_PNG_VALUE)
                            .withBody(getLegendGraphic.getData())
                        ));
                index++;
            }

            return server;
        }
    }

    @Autowired
    private WireMockServer wireMockServer;

    /**
     * After each test method, reset mock server requests
     */
    @AfterEach
    public void reset() {
        this.wireMockServer.resetRequests();
    }

    @AfterAll
    public void tearDown() {
        this.wireMockServer.shutdown();
    }

    @Autowired
    private WmsClient client;

    private static Stream<Arguments> createParameters() {
        return Stream.of(
            Arguments.of("1", "geodata.gov.gr:9603c9bb-fa1c-4272-beb5-2a21204da56b", 2),
            Arguments.of("2", "overlay:ne_10m_admin_0_boundary_lines_land", 2),
            Arguments.of("3", "All European Seas/Water_body_chlorophyll-a.nc*Water body chlorophyll-a", 4)
        );
    }

    @ParameterizedTest
    @MethodSource("createParameters")
    void testWms(int index, String layerName, int expectedRequests) throws Exception {
        assertThat(client).isNotNull();

        final String             data     = ResponsePayload.from(String.format("classpath:results/wms-client/%d.json", index)).getData();
        final ServiceResourceDto expected = this.objectMapper.readValue(data, ServiceResourceDto.class);

        final String  basePath     = wireMockServer.baseUrl();
        final String  relativePath = String.format(PATH_TEMPLATE, index);
        final boolean includeSlash = !basePath.endsWith("/") && !relativePath.startsWith("/");
        final URI     uri          = new URIBuilder(basePath + (includeSlash ? "/" : "") + relativePath)
            .addParameter("SERVICE", "WMS")
            .addParameter("VERSION", "1.3.0")
            .addParameter("REQUEST", "GetCapabilities")
            .build();

        final ServiceResourceDto resource = client.getMetadata(uri.toURL(), layerName);

        // WmsClient executes one GetCapabilities request and zero or more
        // GetLegendGraphic requests
        this.wireMockServer.verify(expectedRequests, WireMock.getRequestedFor(urlPathEqualTo(uri.getPath())));

        assertThat(resource).isNotNull();
        assertThat(resource.getAttribution()).isEqualTo(expected.getAttribution());
        assertThat(resource.getBbox().equalsNorm(expected.getBbox())).isTrue();
        assertThat(resource.getCrs()).containsAll(expected.getCrs());
        assertThat(resource.getMaxScale()).isEqualTo(expected.getMaxScale());
        assertThat(resource.getMinScale()).isEqualTo(expected.getMinScale());
        assertThat(resource.getOutputFormats()).isEqualTo(expected.getOutputFormats());
        assertThat(resource.getServiceType()).isEqualTo(EnumSpatialDataServiceType.WMS);

        JSONAssert.assertEquals(
            objectMapper.writeValueAsString(resource.getStyles()),
            objectMapper.writeValueAsString(expected.getStyles()),
            true
        );
    }

}
