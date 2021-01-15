package eu.opertusmundi.web.model.message.server;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum EnumMessageType {

    /*
     * Unsupported type
     */
    UNDEFINED(0),
    /*
     * Message
     */
    MESSAGE(1),
    /*
     * System notification
     */
    NOTIFICATION(2),
    ;

    private final int value;

    private EnumMessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static EnumMessageType fromString(String value) {
        for (final EnumMessageType item : EnumMessageType.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumMessageType.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumMessageType> {

        @Override
        public EnumMessageType deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumMessageType.fromString(parser.getValueAsString());
        }
    }
}
