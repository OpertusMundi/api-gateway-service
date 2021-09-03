package eu.opertusmundi.web.model.openapi.schema;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.jupyter.client.JupyterConfigurationDto;
import eu.opertusmundi.common.model.jupyter.client.JupyterUserStatusDto;

public class NotebookEndpointTypes {

    public static class ConfigurationResponse extends RestResponse<JupyterConfigurationDto> {

    }

    public static class UserStatusResponse extends RestResponse<JupyterUserStatusDto> {

    }

}
