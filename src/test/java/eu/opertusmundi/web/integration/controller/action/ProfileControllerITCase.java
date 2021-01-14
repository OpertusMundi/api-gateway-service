package eu.opertusmundi.web.integration.controller.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.Message.EnumLevel;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.web.integration.support.AbstractIntegrationTestWithSecurity;
import eu.opertusmundi.web.utils.AccountCommandFactory;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProfileControllerITCase extends AbstractIntegrationTestWithSecurity {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeAll
    public void setupAccounts() {
        // Reset database
        JdbcTestUtils.deleteFromTables(this.jdbcTemplate, "web.account");

        // Create default account with authority ROLE_USER
        final AccountCommandDto command = AccountCommandFactory.user().build();

        this.accountRepository.create(command);
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When loading profile for anonymous user, return 403")
    @WithAnonymousUser
    @Order(1)
    void whenAnonymousGetProfile_return403() throws Exception {
        this.mockMvc.perform(get("/action/profile")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(BasicMessageCode.Forbidden.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.messages[0].description").value(
                this.messageSource.getMessage(BasicMessageCode.Forbidden.key(), null, Locale.getDefault()))
            )
            .andExpect(jsonPath("$.exception").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When updating profile for anonymous user, return 403")
    @WithAnonymousUser
    @Order(2)
    void whenAnonymousUpdateProfile_return403() throws Exception {
        final AccountProfileCommandDto command = new AccountProfileCommandDto();
        command.setFirstName("Demo");
        command.setLastName("User");
        command.setMobile("+306900000000");

        this.mockMvc.perform(post("/action/profile")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(BasicMessageCode.Forbidden.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.messages[0].description").value(
                this.messageSource.getMessage(BasicMessageCode.Forbidden.key(), null, Locale.getDefault()))
            )
            .andExpect(jsonPath("$.exception").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When loading profile for authenticated user, return profile")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    @Order(3)
    void whenAuthenticatedUserGetProfile_returnProfile() throws Exception {
        final MvcResult result = this.mockMvc.perform(get("/action/profile")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();


        final String                          content  = result.getResponse().getContentAsString();
        final RestResponse<AccountProfileDto> response = this.objectMapper.readValue(content,
            new TypeReference<RestResponse<AccountProfileDto>>() { }
        );

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessages()).isEmpty();

        final AccountProfileDto profile = response.getResult();

        assertThat(profile).isNotNull();
        assertThat(profile.getFirstName()).isEqualTo("Demo");
        assertThat(profile.getLastName()).isEqualTo("User");
        assertThat(profile.getLocale()).isEqualTo("en");
        assertThat(profile.getMobile()).isEqualTo("+306900000000");
        assertThat(profile.getModifiedOn()).isNotNull();
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When updating profile for authenticated user, return profile")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    @Order(4)
    void whenAuthenticatedUserUpdateProfile_returnProfile() throws Exception {
        final AccountProfileCommandDto command = AccountCommandFactory.profile()
            .mobile("+306900000001")
            .phone("+302100000001")
            .build();

        this.mockMvc.perform(post("/action/profile")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages").isEmpty())
            .andExpect(jsonPath("$.exception").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())
            .andExpect(jsonPath("$.result").exists())
            .andExpect(jsonPath("$.result.phone").value(command.getPhone()))
            .andExpect(jsonPath("$.result.firstName").value(command.getFirstName()))
            .andExpect(jsonPath("$.result.lastName").value(command.getLastName()))
            .andExpect(jsonPath("$.result.mobile").value(command.getMobile()));
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When updating profile with invalid command, return errors")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    @Order(5)
    void whenAuthenticatedUserUpdateProfileWithInvalidCommand_returnErrors() throws Exception {
        final String                   code    = BasicMessageCode.Validation.key();
        final AccountProfileCommandDto command = new AccountProfileCommandDto();

        this.mockMvc.perform(post("/action/profile")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(2)))
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotEmpty' && @.field == 'lastName')]"
            ).exists())
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotEmpty' && @.field == 'firstName')]"
            ).exists())
            .andExpect(jsonPath("$.result").doesNotExist());
    }

}
