package eu.opertusmundi.web.model.openapi.schema;

import java.util.List;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.spatial.EpsgDto;
import eu.opertusmundi.common.model.spatial.FeatureCollectionDto;
import eu.opertusmundi.common.model.spatial.NutsRegionFeatureDto;
import eu.opertusmundi.common.model.spatial.NutsRegionPropertiesDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class SpatialDataEndpointTypes {

    @Schema(description = "NUTS regions WFS response")
    public static class WfsResponse extends FeatureCollectionDto<NutsRegionFeatureDto> {

    }

    @Schema(description = "NUTS region collection response")
    public static class RegionCollectionResponse extends RestResponse<FeatureCollectionDto<NutsRegionFeatureDto>> {

    }

    @Schema(description = "NUTS region response")
    public static class RegionResponse extends RestResponse<NutsRegionFeatureDto> {

    }

    @Schema(description = "NUTS auto-complete response")
    public static class AutoCompleteRegionResponse extends RestResponse<List<NutsRegionPropertiesDto>> {

    }

    @Schema(description = "EPSG code collection response")
    public static class EpsgCollectionResponse extends RestResponse<List<EpsgDto>> {

    }

    @Schema(description = "Encoding collection response")
    public static class EncodingCollectionResponse extends RestResponse<List<String>> {

    }

}
