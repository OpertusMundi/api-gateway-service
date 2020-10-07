package eu.opertusmundi.web.model.pricing;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "FREE", value = FreePricingModelDto.class),
    @Type(name = "FIXED", value = FixedPricingModelDto.class),
    @Type(name = "SUBSCRIPTION", value = SubscriptionPricingModelDto.class),
})
public abstract class BasePricingModelDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Model unique identifier", example = "a1c04890-9bb9-49f7-a880-6a75e3a561ad")
    @JsonProperty("id")
    @Getter
    @Setter
    protected UUID key;

    @Schema(
        description = "Discriminator field used for deserializing the model to the appropriate data type",
        example = "FREE"
    )
    @JsonDeserialize(using = EnumPricingModel.Deserializer.class)
    @Getter
    @Setter
    protected EnumPricingModel type;

    @Schema(description = "Tax percent", minimum = "0", maximum = "100")
    @Getter
    @Setter
    protected int taxPercent;

    @Schema(description = "Price excluding tax")
    @Getter
    @Setter
    protected BigDecimal totalPriceExcludingTax;

    @Schema(description = "Price tax")
    @Getter
    @Setter
    protected BigDecimal tax;

    @Schema(description = "Price total including tax")
    @Getter
    @Setter
    protected BigDecimal totalPrice;

    @Schema(description = "Currency of monetary values", implementation = String.class, example = "EUR")
    @Getter
    @Setter
    protected Currency currency = Currency.getInstance("EUR");

    protected BasePricingModelDto() {
        this.type = EnumPricingModel.UNDEFINED;
    }

    protected BasePricingModelDto(EnumPricingModel type) {
        this.type = type;
    }

}