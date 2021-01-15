package eu.opertusmundi.web.jackson.joda.money;

import java.io.IOException;

import org.joda.money.Money;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Will serialize a Money value
 *
 * {@link https://gist.github.com/stickfigure/b4d2af290407f9af4cce}
 */
public class MoneySerializer extends JsonSerializer<Money> {

    @Override
    public void serialize(
        final Money value, final JsonGenerator jgen, final SerializerProvider provider
    ) throws IOException, JsonProcessingException {
        final MoneyJson json = new MoneyJson(value.getCurrencyUnit().toString(), value.getAmount());
        jgen.writeObject(json);
    }

}