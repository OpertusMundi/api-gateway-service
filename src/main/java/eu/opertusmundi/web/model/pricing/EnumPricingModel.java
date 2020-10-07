package eu.opertusmundi.web.model.pricing;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum EnumPricingModel {

    /**
     * Invalid pricing model
     */
    UNDEFINED(0),
    /*
     * Free
     */
    FREE(1),
    /**
     * Fixed payment model
     */
    FIXED(2),
    /**
     * Subscription based payment model
     */
    SUBSCRIPTION(3),
    ;

    private final int value;

    private EnumPricingModel(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static EnumPricingModel fromString(String value) {
        for (final EnumPricingModel item : EnumPricingModel.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumPricingModel.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumPricingModel> {

        @Override
        public EnumPricingModel deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumPricingModel.fromString(parser.getValueAsString());
        }
    }
}
