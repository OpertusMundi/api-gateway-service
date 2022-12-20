package eu.opertusmundi.web.controller.api;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceSortField;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceStatus;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceType;
import eu.opertusmundi.common.model.asset.service.UserServiceDto;
import eu.opertusmundi.common.service.UserServiceException;
import eu.opertusmundi.common.service.UserServiceService;
import eu.opertusmundi.web.controller.action.BaseController;

@RestController("ApiUserServiceControllerImpl")
public class UserServiceControllerImpl extends BaseController implements UserServiceController {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceControllerImpl.class);

    private final UserServiceService userServiceService;

    @Autowired
    public UserServiceControllerImpl(UserServiceService userServiceService) {
        this.userServiceService = userServiceService;
    }

    @Override
    public RestResponse<?> findAll(
        Set<EnumUserServiceType> serviceType,
        int pageIndex, int pageSize,
        EnumUserServiceSortField orderBy, EnumSortingOrder order
    ) {
        try {
            final UUID ownerKey  = this.currentUserKey();
            final UUID parentKey = this.currentUserParentKey();

            final PageResultDto<UserServiceDto> result = this.userServiceService.findAll(
                ownerKey, parentKey, Set.of(EnumUserServiceStatus.PUBLISHED), Set.of(EnumUserServiceStatus.DELETED), serviceType,
                pageIndex, pageSize, orderBy, order
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

}
