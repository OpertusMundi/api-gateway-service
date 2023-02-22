package eu.opertusmundi.web.integration.controller.action;

import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.Message.EnumLevel;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceResponse;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.AccountProfileCommandDto;
import eu.opertusmundi.common.model.account.AccountProfileDto;
import eu.opertusmundi.common.model.account.ActivationTokenCommandDto;
import eu.opertusmundi.common.model.account.ActivationTokenDto;
import eu.opertusmundi.common.model.account.EnumActivationStatus;
import eu.opertusmundi.common.model.account.EnumActivationTokenType;
import eu.opertusmundi.common.model.account.PlatformAccountCommandDto;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import eu.opertusmundi.test.support.integration.AbstractIntegrationTestWithSecurity;
import eu.opertusmundi.test.support.utils.ResponsePayload;
import eu.opertusmundi.test.support.utils.ReturnValueCaptor;
import eu.opertusmundi.web.model.security.PasswordChangeCommandDto;
import eu.opertusmundi.web.security.UserService;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountControllerITCase extends AbstractIntegrationTestWithSecurity {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SpyBean
    private UserService userService;

    private final ArgumentCaptor<EnumActivationTokenType> activationTokenType = ArgumentCaptor.forClass(EnumActivationTokenType.class);

    private final ArgumentCaptor<ActivationTokenCommandDto> activationTokenCommand = ArgumentCaptor.forClass(ActivationTokenCommandDto.class);

    private final ReturnValueCaptor<ServiceResponse<ActivationTokenDto>> activationTokenResponse = new ReturnValueCaptor<>();

    /**
     * Configure WireMock server lifecycle events
     */
    @TestConfiguration
    public static class WireMockConfiguration {

        @Value("${opertusmundi.feign.bpm-server.url}")
        private String bpmEngineServiceUrl;

        @Value("${opertusmundi.feign.email-service.url}")
        private String emailService;

        @Autowired
        private ObjectMapper objectMapper;

        @Bean(initMethod = "start", destroyMethod = "stop", name = "bpmEngineService")
        public WireMockServer mockBpmEngineService() throws IOException {
            // Get port from configuration
            final URL url = new URL(this.bpmEngineServiceUrl);

            final WireMockServer server = new WireMockServer(url.getPort());

            server.stubFor(WireMock.post(urlPathEqualTo("/engine-rest/message"))
                .willReturn(WireMock.aResponse()
                    .withStatus(HttpStatus.OK.value())
                ));

            server.stubFor(WireMock
                .post(urlPathEqualTo(String.format(
                    "/engine-rest/process-definition/key/%s/start", EnumWorkflow.ACCOUNT_REGISTRATION.getKey()
                )))
                .willReturn(WireMock.aResponse()
                    .withStatus(HttpStatus.OK.value())
                ));

            final ResponsePayload processInstancesResponse = ResponsePayload.from("classpath:responses/bpm-engine-service/get-process-instance.json");
            server.stubFor(WireMock
                .get(urlPathEqualTo(String.format(
                    "/engine-rest/process-instance", EnumWorkflow.ACCOUNT_REGISTRATION.getKey()
                )))
                .withQueryParam("businessKey", matching("(.+)"))
                .willReturn(WireMock.aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(processInstancesResponse.getData())
                ));

            return server;
        }

        @Bean(initMethod = "start", destroyMethod = "stop", name = "emailService")
        public WireMockServer emailService() throws IOException {
            // Get port from configuration
            final URL url = new URL(this.emailService);

            final WireMockServer server = new WireMockServer(url.getPort());

            final JsonNode body = this.objectMapper.readTree("{\"success\":true,\"messages\":[]}");

            server.stubFor(WireMock.post(urlPathEqualTo("/v1/email/send"))
                .willReturn(WireMock.aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withJsonBody(body)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                ));

            return server;
        }
    }

    @Autowired
    @Qualifier("bpmEngineService")
    private WireMockServer bpmEngineService;

    @Autowired
    @Qualifier("emailService")
    private WireMockServer emailService;

    /**
     * After each test method, reset mock server requests
     */
    @AfterEach
    public void teardown() {
        this.bpmEngineService.resetRequests();
        this.emailService.resetRequests();
    }

    @Test
    @Tag(value = "Controller")
    @Order(10)
    @DisplayName(value = "When register account with invalid request, return errors")
    @Sql(
        scripts = {"classpath:sql/truncate-tables.sql"},
        config = @SqlConfig(separator = ScriptUtils.EOF_STATEMENT_SEPARATOR)
    )
    @Sql(scripts = {
        "classpath:sql/initialize-settings.sql"
    })
    void whenRegisterAccountWithErrors_returnErrors() throws Exception {
        final String code = BasicMessageCode.Validation.key();

        // Empty command
        final AccountProfileCommandDto  profileCommand = new AccountProfileCommandDto();
        final PlatformAccountCommandDto command        = new PlatformAccountCommandDto();

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
            .andExpect(jsonPath("$.messages", hasSize(3)))
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotEmpty' && @.field == 'profile.firstName')]"
            ).exists())
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotEmpty' && @.field == 'profile.lastName')]"
            ).exists())
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotEmpty' && @.field == 'email')]"
            ).exists())
            .andExpect(jsonPath("$.result").doesNotExist());

        // Verify user service
        verify(this.userService, times(0)).createToken(any(EnumActivationTokenType.class), any(), anyBoolean());
    }

    @Test
    @Tag(value = "Controller")
    @Order(11)
    @DisplayName(value = "When register account, return account")
    @Commit
    void whenRegisterAccount_returnNewAccount() throws Exception {
        // Create command
        final String email = TOPIO_USERNAME;

        final AccountProfileCommandDto profileCommand = new AccountProfileCommandDto();
        profileCommand.setFirstName("Demo");
        profileCommand.setLastName("User");
        profileCommand.setLocale("el");
        profileCommand.setMobile("+30690000000");

        final PlatformAccountCommandDto command = new PlatformAccountCommandDto();
        command.setEmail(email);
        command.setPassword(TOPIO_PASSWORD);
        command.setProfile(profileCommand);

        // Capture return value (arguments will be captured in verify)
        doAnswer(this.activationTokenResponse)
            .when(this.userService)
            .createToken(any(EnumActivationTokenType.class), any(), anyBoolean());

        // Send request
        final MvcResult result = this.mockMvc.perform(post("/action/account/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        // Verify user service
        verify(this.userService, times(1)).createToken(this.activationTokenType.capture(), this.activationTokenCommand.capture(), anyBoolean());

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
    @DisplayName(value = "When register account with existing email, return errors")
    void whenRegisterAccountWithExistingEmailAndInvalidPassword_returnErrors() throws Exception {
        final String code = BasicMessageCode.Validation.key();

        // Create command
        final String email = TOPIO_USERNAME;

        final AccountProfileCommandDto profileCommand = new AccountProfileCommandDto();
        profileCommand.setFirstName("Demo");
        profileCommand.setLastName("User");
        profileCommand.setLocale("el");
        profileCommand.setMobile("+30690000000");

        final PlatformAccountCommandDto command = new PlatformAccountCommandDto();
        command.setEmail(email);
        command.setPassword("");
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
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath(
                "$.messages[?(@.code == '" + code + "' && @.description == 'NotUnique' && @.field == 'email')]"
            ).exists())
            .andExpect(jsonPath("$.result").doesNotExist());

        // Verify user service
        verify(this.userService, times(0)).createToken(any(EnumActivationTokenType.class), any(), anyBoolean());
    }

    @Test
    @Order(20)
    @Tag(value = "Controller")
    @DisplayName(value = "When request activation token with invalid command, return errors")
    @Commit
    void whenRequestActivationTokenWithErrors_returnErrors() throws Exception {
        final String code = BasicMessageCode.Validation.key();

        // Create command
        final ActivationTokenCommandDto command = ActivationTokenCommandDto.of("");

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
        verify(this.userService, times(0)).createToken(any(EnumActivationTokenType.class), any(), anyBoolean());
    }

    @Test
    @Order(21)
    @Tag(value = "Controller")
    @DisplayName(value = "When request activation token for unregistered user, return empty response")
    @Commit
    void whenRequestActivationTokenForUnregisteredUser_returnEmptyResponse() throws Exception {
        // Create command
        final ActivationTokenCommandDto command = ActivationTokenCommandDto.of("admin@opertusmundi.eu");

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
        verify(this.userService, times(1)).createToken(any(), anyBoolean());
    }

    @Test
    @Order(22)
    @Tag(value = "Controller")
    @DisplayName(value = "When request activation token, return empty response")
    @Commit
    void whenRequestActivationToken_returnToken() throws Exception {
        final ActivationTokenCommandDto command = ActivationTokenCommandDto.of(TOPIO_USERNAME);

        // Capture return value (arguments will be captured in verify)
        doAnswer(this.activationTokenResponse)
            .when(this.userService)
            .createToken(any(EnumActivationTokenType.class), any(), anyBoolean());

        final MvcResult result = this.mockMvc.perform(post("/action/account/token/request")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        // Verify user service
        verify(this.userService, times(1)).createToken(this.activationTokenType.capture(), this.activationTokenCommand.capture(), anyBoolean());

        // Assert
        assertThat(this.activationTokenType.getValue()).isNull();
        assertThat(this.activationTokenCommand.getValue()).isNotNull();
        assertThat(this.activationTokenResponse.getResult()).isNotNull();
        assertThat(this.activationTokenResponse.getResult().getMessages()).isEmpty();
        assertThat(this.activationTokenResponse.getResult().getResult()).isNotNull();

        final String             content  = result.getResponse().getContentAsString();
        final RestResponse<Void> response = this.objectMapper.readValue(content, new TypeReference<RestResponse<Void>>() { });

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessages()).isEmpty();

        assertEquals(2, this.countRowsInTable("web.activation_token"), "Number of rows in the [web.activation_token] table.");
    }

    @Test
    @Tag(value = "Controller")
    @Order(50)
    @DisplayName(value = "When login failure, redirect")
    void whenLoginFailure_thenRedirect() throws Exception {
        this.mockMvc.perform(formLogin("/login").user(TOPIO_USERNAME).password("invalid"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signin?error=1"));
    }

    @Test
    @Order(51)
    @Tag(value = "Controller")
    @DisplayName(value = "When login success, redirect")
    @Disabled(value = "Requires IDP integration")
    void whenLoginSuccess_thenRedirect() throws Exception {
        this.mockMvc.perform(formLogin("/login").user(TOPIO_USERNAME).password(TOPIO_PASSWORD))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/logged-in"));
    }

    @Test
    @Order(52)
    @Tag(value = "Controller")
    @DisplayName(value = "When login success, return new CSRF token")
    @WithMockUser(username = TOPIO_USERNAME, roles = { "USER" })
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
    @Tag(value = "Controller")
    @DisplayName(value = "When changing password for anonymous user, return 403")
    @WithAnonymousUser
    @Order(60)
    void whenAnonymousUserChangePassword_return403() throws Exception {
        // Create command
        final PasswordChangeCommandDto command = new PasswordChangeCommandDto();
        command.setCurrentPassword(TOPIO_PASSWORD);
        command.setNewPassword("new-password");
        command.setVerifyNewPassword("new-password");

        this.mockMvc.perform(post("/action/account/password/change")
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
            );
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When changing password for authenticated user with invalid credentials, return error")
    @WithUserDetails(value = TOPIO_USERNAME, userDetailsServiceBeanName = "defaultUserDetailsService")
    @Order(61)
    @Disabled(value = "Requires IDP integration")
    void whenAuthenticatedUserChangePasswordWithInvalidCurrentPassword_returnError() throws Exception {
        // Create command
        final PasswordChangeCommandDto command = new PasswordChangeCommandDto();
        command.setCurrentPassword("wrong-password");
        command.setNewPassword("new-password");
        command.setVerifyNewPassword("new-password");

        this.mockMvc.perform(post("/action/account/password/change")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0].code").value(BasicMessageCode.Forbidden.key()))
            .andExpect(jsonPath("$.messages[0].level").value(EnumLevel.ERROR.name()))
            .andExpect(jsonPath("$.messages[0].description").value("Access Denied"))
            .andExpect(jsonPath("$.exception").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    @Tag(value = "Controller")
    @DisplayName(value = "When changing password for authenticated user with valid credentials, return 200")
    @WithUserDetails(value = TOPIO_USERNAME, userDetailsServiceBeanName = "defaultUserDetailsService")
    @Order(62)
    @Disabled(value = "Requires IDP integration")
    void whenAuthenticatedUserChangePasswordWithValidCurrentPassword_return200() throws Exception {
        // Create command
        final String                   email    = TOPIO_USERNAME;
        final String                   password = "new-password";
        final PasswordChangeCommandDto command  = new PasswordChangeCommandDto();

        command.setCurrentPassword(TOPIO_PASSWORD);
        command.setNewPassword("new-password");
        command.setVerifyNewPassword("new-password");

        this.mockMvc.perform(post("/action/account/password/change")
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.objectMapper.writeValueAsString(command)))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.messages").isArray())
            .andExpect(jsonPath("$.messages", hasSize(0)));

        final PasswordEncoder      encoder = new BCryptPasswordEncoder();
        final Optional<AccountDto> account = this.userService.findOneByUserName(email);

        // Asset password is saved
        assertThat(account.isPresent()).isTrue();
        assertThat(encoder.matches(password, account.get().getPassword())).isTrue();
    }

    int countRowsInTable(String tableName) {
        return JdbcTestUtils.countRowsInTable(this.jdbcTemplate, tableName);
    }

}
