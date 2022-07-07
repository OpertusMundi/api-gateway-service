package eu.opertusmundi.web.controller.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.MetadataProperty;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceSortField;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceStatus;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceType;
import eu.opertusmundi.common.model.asset.service.UserServiceCommandDto;
import eu.opertusmundi.common.model.asset.service.UserServiceDto;
import eu.opertusmundi.common.service.UserServiceException;
import eu.opertusmundi.common.service.UserServiceService;
import eu.opertusmundi.web.validation.UserServiceCommandValidator;

@RestController
public class UserServiceControllerImpl extends BaseController implements UserServiceController {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceControllerImpl.class);

    private final UserServiceCommandValidator commandValidator;

    private final UserServiceService userServiceService;

    @Autowired
    public UserServiceControllerImpl(
        UserServiceCommandValidator commandValidator,
        UserServiceService userServiceService
    ) {
        this.commandValidator   = commandValidator;
        this.userServiceService = userServiceService;
    }

    @Override
    public RestResponse<?> findAll(
        Set<EnumUserServiceStatus> status,  Set<EnumUserServiceType> serviceType,
        int pageIndex, int pageSize,
        EnumUserServiceSortField orderBy, EnumSortingOrder order
    ) {
        try {
            final UUID ownerKey  = this.currentUserKey();
            final UUID parentKey = this.currentUserParentKey();

            final PageResultDto<UserServiceDto> result = this.userServiceService.findAll(
                ownerKey, parentKey, status, serviceType, pageIndex, pageSize, orderBy, order
            );

            return RestResponse.result(result);
        } catch (final UserServiceException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<UserServiceDto> findOne(UUID serviceKey) {
        try {
            final UUID ownerKey  = this.currentUserKey();
            final UUID parentKey = this.currentUserParentKey();

            final UserServiceDto service = this.userServiceService.findOne(ownerKey, parentKey, serviceKey);

            if (service == null || service.isDeleted()) {
                return RestResponse.notFound();
            }

            return RestResponse.result(service);
        } catch (final UserServiceException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            return RestResponse.failure();
        }
    }

    @Override
    public RestResponse<UserServiceDto> create(UserServiceCommandDto command, BindingResult validationResult) {
        try {
            command.setOwnerKey(this.currentUserKey());
            command.setParentKey(this.currentUserParentKey());
            command.setUserName(this.currentUserEmail());

            this.commandValidator.validate(command, validationResult);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors());
            }

            final UserServiceDto service = this.userServiceService.create(command);

            return RestResponse.result(service);
        } catch (final UserServiceException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public BaseResponse delete(UUID serviceKey) {
        try {
            final UUID ownerKey  = this.currentUserKey();
            final UUID parentKey = this.currentUserParentKey();

            this.userServiceService.delete(ownerKey, parentKey, serviceKey);

            return RestResponse.success();
        } catch (final UserServiceException ex) {
            return RestResponse.error(ex.getCode(), ex.getMessage());
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);
        }

        return RestResponse.failure();
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getMetadataProperty(
        UUID serviceKey,  String propertyName, HttpServletResponse response
    ) throws IOException {
        final UUID ownerKey  = this.currentUserKey();
        final UUID parentKey = this.currentUserParentKey();

        final MetadataProperty property = this.userServiceService.resolveMetadataProperty(
            ownerKey, parentKey, serviceKey, propertyName
        );

        final File file = property.getPath().toFile();

        String contentType = Files.probeContentType(property.getPath());
        if (contentType == null) {
            contentType = property.getType().getMediaType();
        }

        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
        response.setHeader("Content-Type", contentType);
        if (file.length() < 1024 * 1024) {
            response.setHeader("Content-Length", Long.toString(file.length()));
        }

        final StreamingResponseBody stream = out -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                IOUtils.copyLarge(inputStream, out);
            }
        };

        return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
    }
}
