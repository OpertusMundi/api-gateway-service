package eu.opertusmundi.web.model.openapi.schema;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointGeometryAsGeoJson {

    @Schema(description = "Geometry type")
    EnumGeometryType type;

    @ArraySchema(
        schema = @Schema(description = "Geometry coordinates"),
        minItems = 2
    )
    double[] coordinates;

}