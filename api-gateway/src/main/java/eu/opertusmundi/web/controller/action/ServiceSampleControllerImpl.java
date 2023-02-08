package eu.opertusmundi.web.controller.action;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.catalogue.client.WfsLayerSample;
import eu.opertusmundi.common.model.catalogue.client.WmsLayerSample;
import eu.opertusmundi.common.model.geodata.EnumGeodataWorkspace;
import eu.opertusmundi.common.model.ingest.ResourceIngestionDataDto;
import eu.opertusmundi.common.service.AssetDraftException;
import eu.opertusmundi.common.service.ProviderAssetService;
import eu.opertusmundi.common.service.ogc.GeoServerUtils;
import eu.opertusmundi.common.service.ogc.OgcServiceClientException;
import eu.opertusmundi.common.service.ogc.UserGeodataConfigurationResolver;

@RestController
public class ServiceSampleControllerImpl extends BaseController implements ServiceSampleController {

    @Autowired
    private ProviderAssetService providerAssetService;

    @Autowired
    private UserGeodataConfigurationResolver userGeodataConfigurationResolver;

    @Autowired
    private GeoServerUtils client;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void wms(
        UUID draftKey, UUID resourceKey, String bbox, HttpServletRequest request, HttpServletResponse response
    ) throws IOException, OgcServiceClientException, URISyntaxException {
        response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE);

        final byte[] result = this.getMap(draftKey, resourceKey, bbox);

        if (result == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.getOutputStream().write(result);
        }
    }

    @Override
    public void getSample(
        UUID draftKey, UUID resourceKey, EnumSpatialDataServiceType type, String bbox,
        HttpServletRequest request, HttpServletResponse response
    ) throws IOException, OgcServiceClientException, URISyntaxException {
        try {
            final List<ResourceIngestionDataDto> services = providerAssetService.getServicesFromCache(this.currentUserParentKey(), draftKey);

            if (services == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            final ResourceIngestionDataDto service = services.stream()
                .filter(s -> s.getKey().equals(resourceKey.toString()))
                .findFirst()
                .orElse(null);

            // Service endpoints must exist
            if (service == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Resolve GeoServer URL and user workspace
            final var userGeodataConfig = userGeodataConfigurationResolver.resolveFromUserKey(currentUserParentKey(), EnumGeodataWorkspace.PUBLIC);

            switch (type) {
                case WMS :
                    final List<WmsLayerSample> samples = this.client.getWmsSamples(userGeodataConfig.getUrl(), service, Arrays.asList(this.bboxToGeometry(bbox)));

                    response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(objectMapper.writeValueAsString(samples.get(0)));
                    break;

                case WFS :
                    final List<WfsLayerSample> wfsSamples = this.client.getWfsSamples(
                        userGeodataConfig.getUrl(), userGeodataConfig.getEffectiveWorkspace(), service, Arrays.asList(this.bboxToGeometry(bbox))
                    );

                    response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(objectMapper.writeValueAsString(wfsSamples.get(0)));
                    break;

                default :
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (final AssetDraftException ex) {
            // Either the draft is not found or the data ingestion has not been
            // completed yet
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private byte[] getMap(UUID draftKey, UUID resourceKey, String bbox) throws URISyntaxException, OgcServiceClientException, MalformedURLException {
        try {
            final List<ResourceIngestionDataDto> services = providerAssetService.getServicesFromCache(currentUserParentKey(), draftKey);
            // Ingestion data must exist
            if (services == null) {
                return null;
            }

            final ResourceIngestionDataDto service = services.stream()
                .filter(s -> s.getKey().equals(resourceKey.toString()))
                .findFirst()
                .orElse(null);

            // Service endpoints must exist
            if (service == null) {
                return null;
            }

            // Resolve GeoServer URL and user workspace
            final var userGeodataConfig = userGeodataConfigurationResolver.resolveFromUserKey(currentUserParentKey(), EnumGeodataWorkspace.PUBLIC);

            final ResourceIngestionDataDto.ServiceEndpoint endpoint = service.getEndpointByServiceType(EnumSpatialDataServiceType.WMS);

            return client.getWmsMap(userGeodataConfig.getUrl(), endpoint.getUri(), service.getTableName(), bbox, 256, 256);
        } catch (final AssetDraftException ex) {
            // Either the draft is not found or the data ingestion has not been
            // completed yet
            return null;
        }
    }

    private Geometry bboxToGeometry(String bbox) {
        final Double[] coords = Arrays.stream(bbox.split(",")).limit(4).map(t->Double.parseDouble(t)).toArray(Double[]::new);
        final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        final Geometry        geom            = geometryFactory.createPolygon(new Coordinate[] {
            new Coordinate(coords[0],coords[1]),
            new Coordinate(coords[2],coords[1]),
            new Coordinate(coords[2],coords[3]),
            new Coordinate(coords[0],coords[3]),
            new Coordinate(coords[0],coords[1])
        });

        return geom;
    }

}
