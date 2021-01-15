package eu.opertusmundi.web.integration.controller.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumActivationStatus;
import eu.opertusmundi.common.model.EnumActivationTokenType;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.Message.EnumLevel;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceResponse;
import eu.opertusmundi.common.model.dto.AccountCommandDto;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.model.dto.ActivationTokenCommandDto;
import eu.opertusmundi.common.model.dto.ActivationTokenDto;
import eu.opertusmundi.web.integration.support.AbstractIntegrationTestWithSecurity;
import eu.opertusmundi.web.security.UserService;
import eu.opertusmundi.web.utils.ReturnValueCaptor;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountControllerITCase extends AbstractIntegrationTestWithSecurity {

    @Value("${opertus-mundi.oauth.failure-uri:/error/401}")
    private String failureUri;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SpyBean
    private UserService userService;

    private final ArgumentCaptor<EnumActivationTokenType> activationTokenType = ArgumentCaptor.forClass(EnumActivationTokenType.class);

    private final ArgumentCaptor<ActivationTokenCommandDto> activationTokenCommand = ArgumentCaptor.forClass(ActivationTokenCommandDto.class);

    private final ReturnValueCaptor<ServiceResponse<ActivationTokenDto>> activationTokenResponse = new ReturnValueCaptor<>();

    @Test
    @Tag(value = "Controller")
    @Order(10)
    @DisplayName(value = "When register account with invalid request, return errors")
    void whenRegisterAccountWithErrors_returnErrors() throws Exception {
        final String code = BasicMessageCode.Validation.key();

        // Empty command
        final AccountProfileCommandDto profileCommand = new AccountProfileCommandDto();
        final AccountCommandDto        command        = new AccountCommandDto();

        command.setProfile(profileCommand);

        // Send request
        this.mockMvc.perform(post("/action/account/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(5)))
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotEmpty' && @.field == 'profile.firstName')]"
            ).exists())
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotEmpty' && @.field == 'profile.lastName')]"
            ).exists())
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotEmpty' && @.field == 'verifyPassword')]"
            ).exists())
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotEmpty' && @.field == 'email')]"
            ).exists())
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotEmpty' && @.field == 'password')]"
            ).exists())
            .andExpect(jsonPath("$.result").doesNotExist());

        // Verify user service
        verify(this.userService, times(0)).createToken(any(), any());
    }

    @Test
    @Tag(value = "Controller")
    @Order(11)
    @DisplayName(value = "When register account, return account")
    @Sql(scripts = {"classpath:sql/reset-database.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Commit
    void whenRegisterAccount_returnNewAccount() throws Exception {
        // Create command
        final String email = "user@opertusmundi.eu";

        final AccountProfileCommandDto profileCommand = new AccountProfileCommandDto();
        profileCommand.setFirstName("Demo");
        profileCommand.setLastName("User");
        profileCommand.setLocale("el");
        profileCommand.setMobile("+30690000000");

        final AccountCommandDto command = new AccountCommandDto();
        command.setEmail(email);
        command.setPassword("password");
        command.setVerifyPassword("password");
        command.setProfile(profileCommand);

        // Capture return value (arguments will be captured in verify)
        doAnswer(this.activationTokenResponse)
            .when(this.userService)
            .createToken(any(), any());

        // Send request
        final MvcResult result = this.mockMvc.perform(post("/action/account/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        // Verify user service
        verify(this.userService, times(1)).createToken(this.activationTokenType.capture(), this.activationTokenCommand.capture());

        // Assert
        assertThat(this.activationTokenType.getValue()).isEqualTo(EnumActivationTokenType.ACCOUNT);
        assertThat(this.activationTokenCommand.getValue()).isNotNull();
        assertThat(this.activationTokenResponse.getResult()).isNotNull();
        assertThat(this.activationTokenResponse.getResult().getMessages()).isEmpty();
        assertThat(this.activationTokenResponse.getResult().getResult()).isNotNull();

        final String                   content  = result.getResponse().getContentAsString();
        final RestResponse<AccountDto> response = this.objectMapper.readValue(content, new TypeReference<RestResponse<AccountDto>>() { });

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessages()).isEmpty();

        final AccountDto account = response.getResult();

        assertThat(account).isNotNull();
        assertThat(account.getEmail()).isEqualTo(email);
        assertThat(account.getUsername()).isEqualTo(email);
        assertThat(account.getActivationStatus()).isEqualTo(EnumActivationStatus.PENDING);
        assertThat(account.getActivatedAt()).isNull();
        assertThat(account.getEmailVerifiedAt()).isNull();
        assertThat(account.isEmailVerified()).isFalse();
        assertThat(account.getKey()).isNotNull();
        assertThat(account.getPassword()).isNull();
        assertThat(account.getRegisteredAt()).isNotNull();
        assertThat(account.getRoles()).hasSize(1);
        assertThat(account.getRoles()).contains(EnumRole.ROLE_USER);

        final AccountProfileDto profile = account.getProfile();

        assertThat(profile).isNotNull();
        assertThat(profile.getFirstName()).isEqualTo("Demo");
        assertThat(profile.getLastName()).isEqualTo("User");
        assertThat(profile.getLocale()).isEqualTo("el");
        assertThat(profile.getMobile()).isEqualTo("+30690000000");
        assertThat(profile.getModifiedOn()).isEqualTo(account.getRegisteredAt());
    }

    @Test
    @Tag(value = "Controller")
    @Order(12)
    @DisplayName(value = "When register account with existing email and invalid password, return errors")
    void whenRegisterAccountWithExistingEmailAndInvalidPassword_returnErrors() throws Exception {
        final String code = BasicMessageCode.Validation.key();

        // Create command
        final String email = "user@opertusmundi.eu";

        final AccountProfileCommandDto profileCommand = new AccountProfileCommandDto();
        profileCommand.setFirstName("Demo");
        profileCommand.setLastName("User");
        profileCommand.setLocale("el");
        profileCommand.setMobile("+30690000000");

        final AccountCommandDto command = new AccountCommandDto();
        command.setEmail(email);
        command.setPassword("password1");
        command.setVerifyPassword("password2");
        command.setProfile(profileCommand);

        // Send request
        this.mockMvc.perform(post("/action/account/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(2)))
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotUnique' && @.field == 'email')]"
            ).exists())
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotEqual' && @.field == 'password')]"
            ).exists())
            .andExpect(jsonPath("$.result").doesNotExist());

        // Verify user service
        verify(this.userService, times(0)).createToken(any(), any());
    }

    @Test
    @Order(20)
    @Tag(value = "Controller")
    @DisplayName(value = "When request activation token with invalid command, return errors")
    @Commit
    void whenRequestActivationTokenWithErrors_returnErrors() throws Exception {
        final String code = BasicMessageCode.Validation.key();

        // Create command
        final ActivationTokenCommandDto command = new ActivationTokenCommandDto();

        // Capture return value (arguments will be captured in verify)
        doAnswer(this.activationTokenResponse)
            .when(this.userService)
            .createToken(any(), any());

        this.mockMvc.perform(post("/action/account/token/request")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotEmpty' && @.field == 'email')]"
            ).exists())
            .andExpect(jsonPath("$.result").doesNotExist());

        // Verify user service
        verify(this.userService, times(0)).createToken(any(), any());
    }

    @Test
    @Order(21)
    @Tag(value = "Controller")
    @DisplayName(value = "When request activation token for unregistered user, return empty response")
    @Commit
    void whenRequestActivationTokenForUnregisteredUser_returnEmptyResponse() throws Exception {
        // Create command
        final ActivationTokenCommandDto command = new ActivationTokenCommandDto();
        command.setEmail("admin@opertusmundi.eu");

        // Capture return value (arguments will be captured in verify)
        doAnswer(this.activationTokenResponse)
            .when(this.userService)
            .createToken(any(), any());

        this.mockMvc.perform(post("/action/account/token/request")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages").isEmpty())
            .andExpect(jsonPath("$.result").doesNotExist());

        // Verify user service
        verify(this.userService, times(1)).createToken(any(), any());
    }

    @Test
    @Order(22)
    @Tag(value = "Controller")
    @DisplayName(value = "When request activation token, return empty response")
    @Commit
    void whenRequestActivationToken_returnToken() throws Exception {
        final ActivationTokenCommandDto command = new ActivationTokenCommandDto();
        command.setEmail("user@opertusmundi.eu");

        // Capture return value (arguments will be captured in verify)
        doAnswer(this.activationTokenResponse)
            .when(this.userService)
            .createToken(any(), any());

        final MvcResult result = this.mockMvc.perform(post("/action/account/token/request")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        // Verify user service
        verify(this.userService, times(1)).createToken(this.activationTokenType.capture(), this.activationTokenCommand.capture());

        // Assert
        assertThat(this.activationTokenType.getValue()).isEqualTo(EnumActivationTokenType.ACCOUNT);
        assertThat(this.activationTokenCommand.getValue()).isNotNull();
        assertThat(this.activationTokenResponse.getResult()).isNotNull();
        assertThat(this.activationTokenResponse.getResult().getMessages()).isEmpty();
        assertThat(this.activationTokenResponse.getResult().getResult()).isNotNull();

        final String                   content  = result.getResponse().getContentAsString();
        final RestResponse<Void> response = this.objectMapper.readValue(content, new TypeReference<RestResponse<Void>>() { });

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessages()).isEmpty();

        assertEquals(2, this.countRowsInTable("web.activation_token"), "Number of rows in the [web.activation_token] table.");
    }

    @Test
    @Order(30)
    @Tag(value = "Controller")
    @DisplayName(value = "When token not found, return error")
    @Commit
    void whenActivationTokenNotFound_returnError() throws Exception {
        final UUID token = UUID.randomUUID();

        this.mockMvc.perform(post("/action/account/token/verify/{token}", token)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(BasicMessageCode.TokenNotFound.key()))
            .andExpect(jsonPath("$.result").doesNotExist());

        // Verify
        verify(this.userService, times(1)).redeemToken(token);
    }

    @Test
    @Order(31)
    @Tag(value = "Controller")
    @DisplayName(value = "When verify activation token, return empty response")
    @Commit
    void whenVerifyActivationToken_activateAccount() throws Exception {
        final UUID token = this.activationTokenResponse.getResult().getResult().getToken();

        final MvcResult result = this.mockMvc.perform(post("/action/account/token/verify/{token}", token)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        // Verify
        verify(this.userService, times(1)).redeemToken(token);

        // Assert
        final String             content  = result.getResponse().getContentAsString();
        final RestResponse<Void> response = this.objectMapper.readValue(content, new TypeReference<RestResponse<Void>>() { });

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessages()).isEmpty();
    }

    @Test
    @Order(32)
    @Tag(value = "Controller")
    @DisplayName(value = "When verify expired activation token, return error")
    @Commit
    void whenVerifyExpiredActivationToken_returnError() throws Exception {
        final UUID token = this.activationTokenResponse.getResult().getResult().getToken();

        // Attempt to redeem the same token
        this.mockMvc.perform(post("/action/account/token/verify/{token}", token)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(BasicMessageCode.TokenIsExpired.key()))
            .andExpect(jsonPath("$.result").doesNotExist());

        // Verify
        verify(this.userService, times(1)).redeemToken(token);
    }

    @Test
    @Tag(value = "Controller")
    @Order(50)
    @DisplayName(value = "When login failure, redirect")
    void whenLoginFailure_thenRedirect() throws Exception {
        this.mockMvc.perform(formLogin("/login").user("user@opertusmundi.eu").password("invalid"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(this.failureUri));
    }

    @Test
    @Order(51)
    @Tag(value = "Controller")
    @DisplayName(value = "When login success, redirect")
    void whenLoginSuccess_thenRedirect() throws Exception {
        this.mockMvc.perform(formLogin("/login").user("user@opertusmundi.eu").password("password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/logged-in"));
    }

    @Test
    @Order(52)
    @Tag(value = "Controller")
    @DisplayName(value = "When login success, return new CSRF token")
    @WithMockUser(username = "user@opertusmundi.eu", roles = { "USER" })
    void whenLoginSuccess_thenReturnCsrfToken() throws Exception {
        this.mockMvc.perform(get("/logged-in"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages").isEmpty())
            .andExpect(jsonPath("$.result.csrfHeader").isNotEmpty())
            .andExpect(jsonPath("$.result.csrfHeader").value("X-CSRF-TOKEN"))
            .andExpect(jsonPath("$.result.csrfToken").isNotEmpty());
    }

    @Test
    @Order(53)
    @Tag(value = "Controller")
    @DisplayName(value = "When logout, redirect")
    void whenLogout_thenRedirect() throws Exception {
        this.mockMvc.perform(post("/logout")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/logged-out"));
    }

    @Test
    @Order(54)
    @Tag(value = "Controller")
    @DisplayName(value = "When logout, return new CSRF token")
    void whenLogout_thenReturnCsrfToken() throws Exception {
        this.mockMvc.perform(get("/logged-out"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages").isEmpty())
            .andExpect(jsonPath("$.result.csrfHeader").isNotEmpty())
            .andExpect(jsonPath("$.result.csrfHeader").value("X-CSRF-TOKEN"))
            .andExpect(jsonPath("$.result.csrfToken").isNotEmpty());
    }

    @Test
    @Order(60)
    @Tag(value = "Controller")
    @DisplayName(value = "When get user data for anonymous user, return 403")
    void whenAnonymousGetUserData_return403() throws Exception {
        this.mockMvc.perform(get("/action/account/user-data")
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
    @Order(61)
    @Tag(value = "Controller")
    @DisplayName(value = "When get user data for authenticated user, return user data")
    @WithUserDetails(value = "user@opertusmundi.eu", userDetailsServiceBeanName = "defaultUserDetailsService")
    void whenGetUserData_returnAccount() throws Exception {
        final MvcResult result = this.mockMvc.perform(get("/action/account/user-data")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();


        final String                   content  = result.getResponse().getContentAsString();
        final RestResponse<AccountDto> response = this.objectMapper.readValue(content, new TypeReference<RestResponse<AccountDto>>() { });

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessages()).isEmpty();

        final AccountDto account = response.getResult();

        assertThat(account).isNotNull();
        assertThat(account.getEmail()).isEqualTo("user@opertusmundi.eu");
        assertThat(account.getUsername()).isEqualTo("user@opertusmundi.eu");
        assertThat(account.getActivationStatus()).isEqualTo(EnumActivationStatus.COMPLETED);
        assertThat(account.getActivatedAt()).isNotNull();
        assertThat(account.getEmailVerifiedAt()).isNotNull();
        assertThat(account.isEmailVerified()).isTrue();
        assertThat(account.getKey()).isNotNull();
        assertThat(account.getPassword()).isNull();
        assertThat(account.getRegisteredAt()).isNotNull();
        assertThat(account.getRoles()).hasSize(1);
        assertThat(account.getRoles()).contains(EnumRole.ROLE_USER);

        final AccountProfileDto profile = account.getProfile();

        assertThat(profile).isNotNull();
        assertThat(profile.getFirstName()).isEqualTo("Demo");
        assertThat(profile.getLastName()).isEqualTo("User");
        assertThat(profile.getLocale()).isEqualTo("el");
        assertThat(profile.getMobile()).isEqualTo("+30690000000");
        assertThat(profile.getModifiedOn()).isEqualTo(account.getRegisteredAt());
    }


    int countRowsInTable(String tableName) {
        return JdbcTestUtils.countRowsInTable(this.jdbcTemplate, tableName);
    }

}
