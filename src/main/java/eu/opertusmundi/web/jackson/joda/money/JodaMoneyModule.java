package eu.opertusmundi.web.jackson.joda.money;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;

public class JodaMoneyModule extends Module implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final CurrencyUnit currency;

    public JodaMoneyModule(CurrencyUnit currency) {
        this.currency = currency;

    }

    @Override
    public String getModuleName() {
        return this.getClass().getName();
    }

    @Override
    public Version version() {
        return VersionUtil.parseVersion("1.0.0", "eu.opertusmundi", "opertus-mundi-api-gateway");
    }

    @Override
    public void setupModule(SetupContext context) {
        final SimpleDeserializers desers = new SimpleDeserializers();

        desers.addDeserializer(Money.class, new MoneyDeserializer(this.currency));

        context.addDeserializers(desers);

        final SimpleSerializers sers = new SimpleSerializers();

        sers.addSerializer(Money.class, new MoneySerializer());

        context.addSerializers(sers);
    }

}