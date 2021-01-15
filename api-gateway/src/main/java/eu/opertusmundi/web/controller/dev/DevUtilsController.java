package eu.opertusmundi.web.controller.dev;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.domain.ActivationTokenEntity;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.ActivationTokenDto;
import eu.opertusmundi.common.repository.ActivationTokenRepository;
import eu.opertusmundi.web.model.security.User;
import io.swagger.v3.oas.annotations.Hidden;

@Profile("development")
@Hidden
@RestController
@RequestMapping(path = "/action/dev-utils/", produces = MediaType.APPLICATION_JSON_VALUE)
@Secured({"ROLE_DEVELOPER"})
public class DevUtilsController {

    private static final Logger logger = LoggerFactory.getLogger(DevUtilsController.class);

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @GetMapping(value = "/activation-tokens")
    Object getActiveActivationTokens(Authentication auth) {
        logger.warn("[Dev Utilities] Activation tokens requested for user [{}]", auth.getPrincipal());

        final User details = (User) auth.getPrincipal();

        final List<ActivationTokenDto> r = this.activationTokenRepository.findAllByAccountId(details.getAccount().getId())
            .stream()
            .map(ActivationTokenEntity::toDto)
            .collect(Collectors.toList());

        return RestResponse.result(r);
    }

}
