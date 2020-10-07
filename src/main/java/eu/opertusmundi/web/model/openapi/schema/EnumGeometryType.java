package eu.opertusmundi.web.model.openapi.schema;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum EnumGeometryType {

    Point,
    MultiPoint,
    LineString,
    MultiLineString,
    Polygon,
    MultiPolygon
    ;

}
