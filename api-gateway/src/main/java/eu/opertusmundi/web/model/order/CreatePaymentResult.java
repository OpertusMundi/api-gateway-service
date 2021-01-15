package eu.opertusmundi.web.model.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreatePaymentResult {

    @Schema(
        description = "Stripe account publishable key"
    )
    private final String publishableKey;

    @Schema(
        description = "Client secret associated to the requested payment intent"
    )
    private final String clientSecret;

}
