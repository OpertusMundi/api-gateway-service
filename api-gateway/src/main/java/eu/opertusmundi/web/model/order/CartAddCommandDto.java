package eu.opertusmundi.web.model.order;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CartAddCommandDto {

    @Schema(description = "Catalogue asset unique id")
    private UUID productId;

    @Schema(description = "Pricing model unique id")
    private UUID pricingModelId;

}
