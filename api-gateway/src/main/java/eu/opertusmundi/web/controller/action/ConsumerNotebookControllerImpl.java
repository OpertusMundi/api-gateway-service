package eu.opertusmundi.web.controller.action;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.feign.client.JupyterHubFeignClient;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.jupyter.JupyterHubProfile;
import eu.opertusmundi.common.model.jupyter.JupyterMessageCode;
import eu.opertusmundi.common.model.jupyter.client.JupyterConfigurationDto;
import eu.opertusmundi.common.model.jupyter.client.JupyterUserStatusDto;
import eu.opertusmundi.common.model.jupyter.server.GroupUsersCommandDto;
import eu.opertusmundi.common.model.jupyter.server.ServerDto;
import eu.opertusmundi.common.model.jupyter.server.UserDto;
import eu.opertusmundi.common.model.jupyter.server.UserServerCommandDto;
import eu.opertusmundi.web.config.JupyterHubConfiguration;
import eu.opertusmundi.web.utils.UrlUtils;
import feign.FeignException;

@RestController
public class ConsumerNotebookControllerImpl extends BaseController implements ConsumerNotebookController {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerNotebookControllerImpl.class);

    @Autowired
    private JupyterHubConfiguration jupyterConfiguration;

    @Autowired
    private ObjectProvider<JupyterHubFeignClient> jupyterClient;

    @Override
    public RestResponse<?> getConfiguration() {
        final String                  userName = this.currentUserEmail();
        final UserDto                 user     = this.jupyterClient.getObject().getUser(userName).getBody();

        final JupyterConfigurationDto result   = JupyterConfigurationDto.builder()
            .profiles(this.jupyterConfiguration.getProfiles())
            .groups(user.getGroups())
            .activeProfile(user.getServerForDefaultName().map(ServerDto::getProfileName).orElse(null))
            .build();

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> startServer(String profileName) {
        try {
            final String            userName = this.currentUserEmail();
            final JupyterHubProfile profile  = jupyterConfiguration.getProfileByName(profileName);

            // The profile must exist
            if (profile == null) {
                return RestResponse.error(JupyterMessageCode.PROFILE_NOT_FOUND, "Profile not found");
            }

            // Get current user status
            UserDto user = this.jupyterClient.getObject().getUser(userName).getBody();

            // Do not start a new server if one already exists
            if (user.getServerForDefaultName().isPresent()) {
                return RestResponse.error(JupyterMessageCode.SERVER_RUNNING, "A server is already running");
            }

            // TODO: Currently, we add the user to any required group!
            final String groupName = profile.getGroups().get(profile.getGroups().size() - 1);

            if (!user.getGroups().contains(groupName)) {
                final GroupUsersCommandDto groupCommand = new GroupUsersCommandDto();
                groupCommand.setUsers(Arrays.asList(userName));

                this.jupyterClient.getObject().addUsersToGroup(groupName, groupCommand);
            }

            final UserServerCommandDto serverCommand = new UserServerCommandDto();
            serverCommand.setProfileName(profileName);

            this.jupyterClient.getObject().startServerForUser(userName, serverCommand);

            // Refresh user status
            user = this.jupyterClient.getObject().getUser(userName).getBody();

            final ServerDto server = user.getServerForDefaultName().orElse(null);

            if (server != null) {
                final JupyterUserStatusDto result = this.getServerStatus(user, server);
                return RestResponse.result(result);
            }

            return RestResponse.error(JupyterMessageCode.API_ERROR, "No server spawned");
        } catch (final FeignException ex) {
            logger.error(String.format("Failed to start notebook server [profile=%s]", profileName), ex);

            return RestResponse.error(JupyterMessageCode.API_ERROR, "Jupyter operation has failed");
        }
    }

    @Override
    public RestResponse<?> getServerStatus() {
        try {
            final String userName = this.currentUserEmail();

            // Get current user status
            final UserDto   user   = this.jupyterClient.getObject().getUser(userName).getBody();
            final ServerDto server = user.getServerForDefaultName().orElse(null);

            if (server != null) {
                final JupyterUserStatusDto result = this.getServerStatus(user, server);
                return RestResponse.result(result);
            }

            return RestResponse.result(JupyterUserStatusDto.empty());
        } catch (final FeignException ex) {
            logger.error("Failed to query notebook server status", ex);

            return RestResponse.error(JupyterMessageCode.API_ERROR, "Jupyter operation has failed");
        }
    }

    @Override
    public RestResponse<Void> stopServer() {
        try {
            final String userName = this.currentUserEmail();

            // Get current user status
            final UserDto   user   = this.jupyterClient.getObject().getUser(userName).getBody();
            final ServerDto server = user.getServerForDefaultName().orElse(null);

            if (server != null) {
                this.jupyterClient.getObject().stopServerForUser(userName);

                return RestResponse.success();
            }

            return RestResponse.error(JupyterMessageCode.SERVER_NOT_RUNNING, "No server running");
        } catch (final FeignException ex) {
            logger.error("Failed to stop notebook server", ex);

            return RestResponse.error(JupyterMessageCode.API_ERROR, "Jupyter operation has failed");
        }
    }

    private JupyterUserStatusDto getServerStatus(UserDto user, ServerDto server) {
        final var baseUrl    = this.jupyterConfiguration.getUrl().toString();
        final var serverPath = server.getUrlPath();
        final var serverUrl  = UrlUtils.join(baseUrl, serverPath);

        final JupyterUserStatusDto result = JupyterUserStatusDto.builder()
            .ready(server.isReady())
            .path(serverUrl)
            .profile(user.getServerForDefaultName().map(ServerDto::getProfileName).get())
            .build();

        return result;
    }
}
