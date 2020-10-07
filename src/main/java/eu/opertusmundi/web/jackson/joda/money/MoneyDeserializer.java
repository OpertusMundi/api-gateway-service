package eu.opertusmundi.web.jackson.joda.money;

import java.io.IOException;
import java.math.BigDecimal;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Will deserialize a Money that was serialized with the MoneySerializer. Also
 * understands a simple decimal as the default currency unit.
 *
 * {@link https://gist.github.com/stickfigure/b4d2af290407f9af4cce}
 */
public class MoneyDeserializer extends StdDeserializer<Money> {

    private static final long serialVersionUID = 1L;

    private final CurrencyUnit currency;

    public MoneyDeserializer(CurrencyUnit currency) {
        super(Money.class);

        this.currency = currency;
    }

    @Override
    public Money deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (jp.getCurrentToken().isNumeric()) {
            // For numeric values, use the default currency
            final BigDecimal amount = jp.getDecimalValue();
            return Money.of(this.currency, amount);
        } else if (jp.getCurrentToken().isStructStart()) {
            final MoneyJson json = jp.readValueAs(MoneyJson.class);

            final CurrencyUnit currency = json.getCurrency() == null ? this.currency : CurrencyUnit.of(json.getCurrency());

            if (json.getAmount() != null) {
                return Money.of(currency, json.getAmount());
            }

            throw new IOException("Money structure needs 'amount'");
        } else {
            throw new IOException("Expected either a number or a money structure");
        }
    }
}
