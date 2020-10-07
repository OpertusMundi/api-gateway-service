package eu.opertusmundi.web.model.openapi.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(oneOf = {PointGeometryAsGeoJson.class, LineStringGeometryAsGeoJson.class, PolygonGeometryAsGeoJson.class})
public class GeometryAsJson {

}