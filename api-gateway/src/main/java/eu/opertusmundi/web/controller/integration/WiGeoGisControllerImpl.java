package eu.opertusmundi.web.controller.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.service.integration.WiGeoGisSessionManager;
import eu.opertusmundi.web.controller.action.BaseController;

@RestController
@ConditionalOnProperty(name = "opertusmundi.feign.wigeogis.url", matchIfMissing = true)
public class WiGeoGisControllerImpl extends BaseController implements WiGeoGisController {

    private final WiGeoGisSessionManager sessionManager;

    @Autowired
    public WiGeoGisControllerImpl(WiGeoGisSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public RestResponse<?> login() {
        final var session = this.sessionManager.login(this.currentUserId());

        return RestResponse.result(session);
    }

}
