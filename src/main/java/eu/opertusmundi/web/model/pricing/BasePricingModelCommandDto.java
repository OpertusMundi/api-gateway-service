package eu.opertusmundi.web.model.pricing;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "FREE", value = FreePricingModelCommandDto.class),
    @Type(name = "FIXED", value = FixedPricingModelCommandDto.class),
    @Type(name = "SUBSCRIPTION", value = SubscriptionPricingModelCommandDto.class),
})
public abstract class BasePricingModelCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected BasePricingModelCommandDto() {
        this.type = EnumPricingModel.UNDEFINED;
    }

    protected BasePricingModelCommandDto(EnumPricingModel type) {
        this.type = type;
    }

    /**
     * Pricing model unique key. The value is always generated at the server and
     * any value specified by the API gateway client is ignored.
     */
    @Schema(description = "Model unique identifier", example = "a1c04890-9bb9-49f7-a880-6a75e3a561ad")
    @Hidden
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

}