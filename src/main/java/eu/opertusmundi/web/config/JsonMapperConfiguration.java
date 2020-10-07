package eu.opertusmundi.web.config;

import org.joda.money.CurrencyUnit;
import org.locationtech.spatial4j.io.jackson.ShapesAsGeoJSONModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import eu.opertusmundi.web.jackson.joda.money.JodaMoneyModule;;

@Configuration
public class JsonMapperConfiguration {

    /**
     * Creates an instance of {@link ObjectMapper} and configures support for spatial
     * objects serialization
     *
     * @param builder a builder used to create {@link ObjectMapper} instances with a fluent API
     * @return a configured instance of {@link ObjectMapper}
     */
	@Bean
	@Primary
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        final ObjectMapper objectMapper = builder.build();

        objectMapper.registerModule(new ShapesAsGeoJSONModule());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new JodaMoneyModule(CurrencyUnit.EUR));

        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        objectMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

        return objectMapper;
	}

}
