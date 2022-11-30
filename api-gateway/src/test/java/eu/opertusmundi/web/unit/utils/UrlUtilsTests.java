package eu.opertusmundi.web.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.opertusmundi.web.utils.UrlUtils;

@ExtendWith(SpringExtension.class)
public class UrlUtilsTests {

    private static String SERVER_URL = "https://jupyterhub.beta.topio.market/user@opertusmundi.eu/sever-01";

    private static Stream<Arguments> createParameters() {
        return Stream.of(
            Arguments.of("https://jupyterhub.beta.topio.market/", "/user@opertusmundi.eu/sever-01"),
            Arguments.of("https://jupyterhub.beta.topio.market/", "user@opertusmundi.eu/sever-01"),
            Arguments.of("https://jupyterhub.beta.topio.market",  "/user@opertusmundi.eu/sever-01"),
            Arguments.of("https://jupyterhub.beta.topio.market",  "user@opertusmundi.eu/sever-01")
        );
    }

    @ParameterizedTest
    @MethodSource("createParameters")
    void testJoin(String url1, String Url2) throws Exception {
        final var result = UrlUtils.join(url1, Url2);

        assertThat(result).isEqualTo(SERVER_URL);
    }
}
