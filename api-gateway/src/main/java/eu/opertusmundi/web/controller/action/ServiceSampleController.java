package eu.opertusmundi.web.controller.action;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.catalogue.client.WmsLayerSample;
import eu.opertusmundi.common.service.ogc.OgcServiceClientException;
import eu.opertusmundi.web.model.openapi.schema.EndpointTags;
import eu.opertusmundi.web.model.openapi.schema.ServiceSamplesEndpointTypes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name        = EndpointTags.Samples,
    description = "The API for creating samples from WMS/WFS layers"
)
@RequestMapping(path = "/action", produces = MediaType.APPLICATION_JSON_VALUE)
@Secured({"ROLE_PROVIDER", "ROLE_VENDOR_PROVIDER"})
public interface ServiceSampleController {

   /**
    * WMS service
    *
    * @param draftKey
    * @param resourceKey
    * @param bbox
    * @param request
    * @param response
    * @throws IOException if controller fails to write image to the response
    * @throws OgcServiceClientException if WMS GetMap request fails to execute
    * @throws URISyntaxException if service endpoint is malformed
    */
    @Operation(
        operationId = "draft-samples-01",
        summary     = "WMS service",
        description = "Implements a pseudo WMS service for the service resources of assets with type "
                    + "`SERVICE`. Only services of type `WMS` and `WMS` are supported. The endpoint "
                    + "ignores all WMS/WFS parameters except for `BBOX` (bounding box). The CRS is "
                    + "always assumed to be `EPSG:4326`. "
                    + "Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
    )
    @ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = @Content(schema = @Schema(type = "string", format = "binary", description = "The tile image"))
    )
    @GetMapping(value = "/drafts/{draftKey}/resources/{resourceKey}/wms", produces = MediaType.IMAGE_PNG_VALUE)
    void wms(
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Draft unique key. The draft must be an asset of type `SERVICE` with "
                        + "`spatialDataServiceType` in [`WMS`, `WFS`]"
        )
        @PathVariable UUID draftKey,
        @Parameter(
            in          = ParameterIn.PATH,
            required    = true,
            description = "Resource unique key. The resource must be a service resource."
        )
        @PathVariable UUID resourceKey,
        @Parameter(
            in = ParameterIn.QUERY,
            required = true,
            description = "Bounding box"
        )
        @RequestParam(name = "bbox") String bbox,
        @Parameter(hidden = true)
        HttpServletRequest request,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws IOException, OgcServiceClientException, URISyntaxException;

    /**
     * Get service sample
     *
     * @param draftKey
     * @param resourceKey
     * @param bbox
     * @param request
     * @param response
     * @throws IOException if controller fails to write image to the response
     * @throws OgcServiceClientException if WMS GetMap request fails to execute
     * @throws URISyntaxException if service endpoint is malformed
     */
     @Operation(
         operationId = "draft-samples-02",
         summary     = "Get sample",
         description = "Creates a sample for a `WMS` or `WFS` service. Required role: `ROLE_PROVIDER`, `ROLE_VENDOR_PROVIDER`"
     )
     @ApiResponse(
         responseCode = "200",
         description = "successful operation",
         content = { @Content(
             mediaType = MediaType.APPLICATION_JSON_VALUE,
             schema = @Schema(oneOf = {WmsLayerSample.class, ServiceSamplesEndpointTypes.WfsLayerSampleType.class})
         )}
     )
     @GetMapping(
         value = "/drafts/{draftKey}/resources/{resourceKey}/{type}/samples",
         produces = {MediaType.APPLICATION_JSON_VALUE}
     )
     void getSample(
         @Parameter(
             in          = ParameterIn.PATH,
             required    = true,
             description = "Draft unique key. The draft must be an asset of type `SERVICE` with "
                         + "`spatialDataServiceType` in [`WMS`, `WFS`]"
         )
         @PathVariable UUID draftKey,
         @Parameter(
             in          = ParameterIn.PATH,
             required    = true,
             description = "Resource unique key. The resource must be a service resource."
         )
         @PathVariable UUID resourceKey,
         @Parameter(
             in          = ParameterIn.PATH,
             required    = true,
             description = "Service type."
         )
         @PathVariable EnumSpatialDataServiceType type,
         @Parameter(
             in = ParameterIn.QUERY,
             required = true,
             description = "Bounding box"
         )
         @RequestParam(name = "bbox") String bbox,
         @Parameter(hidden = true)
         HttpServletRequest request,
         @Parameter(hidden = true)
         HttpServletResponse response
     ) throws IOException, OgcServiceClientException, URISyntaxException;

}
