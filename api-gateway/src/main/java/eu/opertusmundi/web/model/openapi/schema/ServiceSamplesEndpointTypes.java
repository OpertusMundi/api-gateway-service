package eu.opertusmundi.web.model.openapi.schema;

import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;

import eu.opertusmundi.common.model.openapi.schema.GeometryAsJson;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class ServiceSamplesEndpointTypes {

    @Schema(description = "WFS layer sample")
    @Getter
    @Setter
    public static class WfsLayerSampleType {

        @Schema(implementation = GeometryAsJson.class, description = "Sample bounding box")
        private Geometry bbox;

        @Schema(description = "Selected features")
        private FeatureCollection data;

        @Schema(description = "Feature")
        @Getter
        @Setter
        public static class Feature {

            @Schema(description = "Object type. Always equal to `Feature`")
            @Getter
            private String type;

            @Schema(description = "Feature unique identifier")
            @Getter
            @Setter
            private String id;

            @Schema(implementation = GeometryAsJson.class, description = "Feature geometry")
            @Getter
            @Setter
            private Geometry geometry;

            @Schema(description = "Feature properties")
            @Getter
            @Setter
            private Map<String, Object> properties;

        }

        @Schema(description = "FeatureCollection")
        @Getter
        @Setter
        public static class FeatureCollection {

            @Schema(description = "Object type. Always equal to `FeatureCollection`")
            @Getter
            private String type;

            @ArraySchema(
                arraySchema = @Schema(
                    description = "Sample features"
                ),
                minItems = 0,
                uniqueItems = true,
                schema = @Schema(implementation = Feature.class)
            )
            @Getter
            @Setter
            private List<Feature> features;

            @Schema(description = "Total number of features")
            @Getter
            @Setter
            private long totalFeatures;

            @Schema(description = "Number of selected features")
            @Getter
            @Setter
            private long numberMatched;

            @Schema(description = "Number of returned features")
            @Getter
            @Setter
            private long numberReturned;

            @Schema(description = "Size (bytes) of the WFS service")
            @Getter
            @Setter
            private long size;

        }
    }

}
