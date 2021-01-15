package eu.opertusmundi.web.model.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OsmConfiguration {

    @Schema(
        description = "OSM endpoint template"
    )
    private String url;

}
