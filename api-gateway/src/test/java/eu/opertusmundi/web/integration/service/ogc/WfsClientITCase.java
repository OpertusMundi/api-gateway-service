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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.service.ogc.WfsClient;
import eu.opertusmundi.web.utils.ResponsePayload;

@SpringBootTest
@ActiveProfiles("testing")
@TestInstance(Lifecycle.PER_CLASS)
public class WfsClientITCase {

    private final static String pathTemplate = "/wfs/%d";

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Configure WireMock server lifecycle events
     */
    @TestConfiguration
    public static class WireMockConfiguration {

        @Value("${opertusmundi.geoserver.endpoint}")
        private String wfsServerUrl;

        @Bean(initMethod = "start", destroyMethod = "stop")
        public WireMockServer mockWfsServer() throws IOException {
            // Get port from configuration
            final URL url = new URL(this.wfsServerUrl);

            final WireMockServer server = new WireMockServer(url.getPort());

            final ResponsePayload[] getCapabilities =  {
                ResponsePayload.from("classpath:responses/wfs-service/1.xml"),
                ResponsePayload.from("classpath:responses/wfs-service/2.xml"),
                ResponsePayload.from("classpath:responses/wfs-service/3.xml"),
            };

            int index = 1;

            for (final ResponsePayload r : getCapabilities) {
                server.stubFor(WireMock.get(urlPathEqualTo(String.format(pathTemplate, index)))
                    .withQueryParam("SERVICE", WireMock.equalToIgnoreCase("WFS"))
                    .withQueryParam("VERSION", WireMock.equalToIgnoreCase("2.0.0"))
                    .withQueryParam("REQUEST", WireMock.equalToIgnoreCase("GetCapabilities"))
                    .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_XML_VALUE)
                        .withBody(r.getData())
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
    private WfsClient client;

    private static Stream<Arguments> createParameters() {
        return Stream.of(
            Arguments.of("1", "gn", "GN.GeographicalNames"),
            Arguments.of("2", "emodnet_chemistry" , "polygons"),
            Arguments.of("3", "geodata.gov.gr" , "4f097ff9-4fbb-4411-86f3-1e7e621df61a")
        );
    }

    @ParameterizedTest
    @MethodSource("createParameters")
    void testWfs(int index, String workspace, String typeName) throws Exception {
        assertThat(client).isNotNull();

        final String             data     = ResponsePayload.from(String.format("classpath:results/wfs-client/%d.json", index)).getData();
        final ServiceResourceDto expected = this.objectMapper.readValue(data, ServiceResourceDto.class);

        final String  basePath     = wireMockServer.baseUrl();
        final String  relativePath = String.format(pathTemplate, index);
        final boolean includeSlash = !basePath.endsWith("/") && !relativePath.startsWith("/");
        final URI     uri          = new URIBuilder(basePath + (includeSlash ? "/" : "") + relativePath)
            .addParameter("SERVICE", "WFS")
            .addParameter("VERSION", "2.0.0")
            .addParameter("REQUEST", "GetCapabilities")
            .build();

        final ServiceResourceDto resource = client.GetMetadata(uri.toString(), workspace, typeName);

        this.wireMockServer.verify(1, WireMock.getRequestedFor(urlPathEqualTo(uri.getPath())));

        assertThat(resource).isNotNull();
        assertThat(resource.getAttribution()).isEqualTo(expected.getAttribution());
        assertThat(resource.getBbox().equalsNorm(expected.getBbox())).isTrue();
        assertThat(resource.getCrs()).isEqualTo(expected.getCrs());
        assertThat(resource.getFilterCapabilities()).isEqualTo(expected.getFilterCapabilities());
        assertThat(resource.getMaxScale()).isEqualTo(expected.getMaxScale());
        assertThat(resource.getMinScale()).isEqualTo(expected.getMinScale());
        assertThat(resource.getOutputFormats()).isEqualTo(expected.getOutputFormats());
        assertThat(resource.getStyles()).isEqualTo(expected.getStyles());
        assertThat(resource.getServiceType()).isEqualTo(EnumSpatialDataServiceType.WFS);
    }

}
