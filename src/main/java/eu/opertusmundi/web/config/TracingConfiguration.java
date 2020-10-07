package eu.opertusmundi.web.config;

import java.util.regex.Pattern;

import org.springframework.cloud.sleuth.instrument.web.HttpServerSampler;
import org.springframework.cloud.sleuth.instrument.web.SkipPatternProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import brave.http.HttpRequest;
import brave.sampler.SamplerFunction;

@Configuration
public class TracingConfiguration {

    @Bean(name = HttpServerSampler.NAME)
    SamplerFunction<HttpRequest> CustomHttpSampler(SkipPatternProvider provider) {
        // Keep default behavior
        final Pattern skipPattern = provider.skipPattern();
        // Trace only action requests
        final Pattern actionPattern = Pattern.compile("^/action/.*");

        return request -> {
            final String  url        = request.path();
            final boolean shouldSkip = skipPattern.matcher(url).matches() || !actionPattern.matcher(url).matches();

            if (shouldSkip) {
                return false;
            }
            return null;
        };
    }

}
