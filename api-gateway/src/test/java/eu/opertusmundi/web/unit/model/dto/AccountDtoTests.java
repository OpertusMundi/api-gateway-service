package eu.opertusmundi.web.unit.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.dto.AccountDto;

@JsonTest
@ExtendWith(SpringExtension.class)
public class AccountDtoTests {

    @Autowired
    private JacksonTester<AccountDto> json;

    /*
    @BeforeEach
    public void setup() {
        final ObjectMapper objectMapper = new ObjectMapper();
        JacksonTester.initFields(this, objectMapper);
    }
    */

    @Test
    void testSerialize() throws Exception {
        final AccountDto account = new AccountDto();
        account.setId(1);
        account.setEmail("demo.user@opertusmundi.eu");
        account.setPassword("password");

        // Id and password properties should not be serialized
        assertThat(this.json.write(account)).doesNotHaveJsonPath("@.id");
        assertThat(this.json.write(account)).doesNotHaveJsonPath("@.password");

        assertThat(this.json.write(account)).hasJsonPathStringValue("@.email");
        assertThat(this.json.write(account)).extractingJsonPathStringValue("@.email").isEqualTo("demo.user@opertusmundi.eu");
    }

    @Test
    void testDeserialize() throws Exception {
        final AccountDto account = new AccountDto();
        account.setId(null);
        account.setEmail("demo.user@opertusmundi.eu");
        account.setPassword(null);
        account.setRoles(new HashSet<EnumRole>());

        final String content = "{\"id\":1,\"password\":\"password\",\"email\":\"demo.user@opertusmundi.eu\",\"roles\":[]}";

        assertThat(this.json.parse(content)).usingRecursiveComparison().isEqualTo(account);
        assertThat(this.json.parseObject(content).getId()).isNull();
        assertThat(this.json.parseObject(content).getPassword()).isNull();
        assertThat(this.json.parseObject(content).getEmail()).isEqualTo("demo.user@opertusmundi.eu");
    }

}
