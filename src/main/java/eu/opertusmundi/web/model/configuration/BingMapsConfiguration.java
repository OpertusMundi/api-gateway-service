package eu.opertusmundi.web.model.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BingMapsConfiguration {

    @Schema(
        description = "Bing Maps application key"
    )
    private String applicationKey;

    @Schema(
        description = "Bing Maps imagery set"
    )
    private String imagerySet;

}
